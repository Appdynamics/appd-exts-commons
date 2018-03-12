/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.http.*;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.xml.Xml;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.javafx.fxml.builder.URLBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {
    public static final Logger logger = LoggerFactory.getLogger(CustomDashboardUploader.class);

    public void uploadDashboard(String dashboardName, Xml xml, Map<String, ? super Object> argsMap, boolean overwrite) {
        setProxyIfApplicable(argsMap);
        CloseableHttpClient client = Http4ClientBuilder.getBuilder(argsMap).build();
        //SimpleHttpClient client = new SimpleHttpClientBuilder(argsMap).connectionTimeout(10000).socketTimeout(15000).build();
        try {
            //path("controller/auth?action=login")
            List<Map<String, ?>> serversList = (List<Map<String, ?>>)argsMap.get("servers");
            Map<String, ?> serverMap = (Map)serversList.iterator().next();
            Map<String, String> serverStringMap = new HashMap<>();
            serverStringMap.put(TaskInputArgs.HOST, (String)serverMap.get(TaskInputArgs.HOST));
            serverStringMap.put(TaskInputArgs.PORT, (String)serverMap.get(TaskInputArgs.PORT));
            serverStringMap.put(TaskInputArgs.USE_SSL, String.valueOf(serverMap.get(TaskInputArgs.USE_SSL)));

            HttpGet get = new HttpGet(UrlBuilder.builder(serverStringMap).path("controller/auth?action=login").build());
            HttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null && statusLine.getStatusCode() == 200) {
                Header[] headers = response.getAllHeaders();
                StringBuilder cookies = new StringBuilder();
                String csrf = null;
                for (Header header : headers) {
                    if (header.getName().equalsIgnoreCase("set-cookie")) {
                        String value = header.getValue();
                        cookies.append(value).append(";");
                        if (value.toLowerCase().contains("x-csrf-token")) {
                            csrf = value.split("=")[1];
                            if (csrf.contains(";")) {
                                csrf = csrf.split(";")[0].trim();
                            }
                        }
                    }
                }
                logger.debug("The controller login is successful, the cookie is [{}] and csrf is {}", cookies, csrf);
                boolean isPresent = isDashboardPresent(client, cookies, dashboardName, csrf, argsMap, serverStringMap);
                if (isPresent) {
                    if (overwrite) {
                        //#TODO Eventhough we intend to overwrite, this will actually create a new dashboard.
                        uploadFile(dashboardName, xml, argsMap, serverStringMap, cookies, csrf);
                    } else {
                        logger.debug("The dashboard {} exists or API has been changed, not processing dashboard upload", dashboardName);
                    }
                } else {
                    uploadFile(dashboardName, xml, argsMap, serverStringMap, cookies, csrf);
                }
            } else if(statusLine!= null) {
                logger.error("Custom Dashboard Upload Failed. The login to the controller is unsuccessful. The response code is {}"
                        , statusLine.getStatusCode());
                logger.error("The response headers are {} and content is {}", Arrays.toString(response.getAllHeaders()), response.getEntity());
            }
        } catch (Exception e){
          logger.error(e.getMessage());
        } finally {
            try {
                client.close();
            }
            catch (Exception e){
                logger.error(e.getMessage());
            }
        }
    }

    private void setProxyIfApplicable(Map<String, ? super Object> argsMap) {
        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");
        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {
            Map<String, ? super Object> proxyMap = new HashMap<>();
            proxyMap.put(TaskInputArgs.HOST, proxyHost);
            proxyMap.put(TaskInputArgs.PORT, proxyPort);
            argsMap.put("proxy", proxyMap);
            logger.debug("Using the proxy {}:{} to upload the dashboard", proxyHost, proxyPort);
        } else {
            logger.debug("Not using proxy for dashboard upload appdynamics.http.proxyHost={} and appdynamics.http.proxyPort={}"
                    , proxyHost, proxyPort);
        }
    }

    private boolean isDashboardPresent(CloseableHttpClient client, StringBuilder cookies, String dashboardName, String csrf, Map<String, ?> argsMap, Map<String, String> serverStringMap) {
        try {
            HttpGet get = new HttpGet(UrlBuilder.builder(serverStringMap).path("controller/restui/dashboards/getAllDashboardsByType/false").build());
            get.setHeader("Cookie", cookies.toString());
            get.setHeader("X-CSRF-TOKEN", csrf);
            HttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null && statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                JsonNode arrayNode = new ObjectMapper().readValue(EntityUtils.toString(entity), JsonNode.class);
                boolean isPresent = false;
                if (arrayNode != null) {
                    for (JsonNode jsonNode : arrayNode) {
                        String name = getTextValue(jsonNode.get("name"));
                        if (dashboardName.equals(name)) {
                            isPresent = true;
                        }
                    }
                }
                return isPresent;
            } else if(statusLine != null) {
                logger.error("The controller API [isDashboardPresent] returned invalid response{}, so cannot upload the dashboard"
                        , statusLine.getStatusCode());
                logger.info("Please change the [uploadDashboard] property in the config.yml to false. " +
                        "The xml will be written to the logs folder. Please import it to controller manually");
                logger.error("This API was changed in the controller version 4.3. So for older controllers, upload the dashboard xml file from the logs folder.");
                return true;//Fake that the dashboard exists.
            }
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return false;
    }

    private void uploadFile(String instanceName, Xml xml, Map<String, ?> argsMap, Map<String, String> serverStringMap, StringBuilder cookies, String csrf) {
        try {
            uploadFile(instanceName, xml, cookies, argsMap, serverStringMap, csrf);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    //#TODO use the same httpClient created above to call CustomDashboardImportExportServlet.
    public void uploadFile(String dashboardName, Xml xml, StringBuilder cookies, Map<String, ?> argsMap, Map<String, String> serverStringMap, String csrf) throws IOException {
        String fileName = dashboardName + ".xml";
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";

        String urlStr = new UrlBuilder(serverStringMap).path("controller/CustomDashboardImportExportServlet").build();
        logger.info("Uploading the custom Dashboard {} to {}", dashboardName, urlStr);

        HttpURLConnection connection = null;
        URL url = new URL(urlStr);
        if (argsMap.containsKey("proxy")) {
            Map<String, ?> proxyMap = (Map<String, ?>)argsMap.get("proxy");
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((String)proxyMap.get(TaskInputArgs.HOST)
                    , Integer.parseInt((String)proxyMap.get(TaskInputArgs.PORT))));
            connection = (HttpURLConnection) url.openConnection(proxy);
            logger.debug("Created an HttpConnection for Fileupload with a proxy {}", proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
            httpsURLConnection.setSSLSocketFactory(createSSLSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CustomHostnameVerifier());
        }
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        connection.setRequestProperty("Cookie", cookies.toString());
        DataOutputStream request = new DataOutputStream(connection.getOutputStream());

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        /*builder.addTextBody();
        builder.addTextBody();
        builder.addTextBody();*/

        request.writeBytes(twoHyphens + boundary + lineEnd);
        request.writeBytes("Content-Disposition: form-data; name=\"" + dashboardName + "\";filename=\"" + fileName + "\"" + lineEnd);
        request.writeBytes("Content-Type: text/xml" + lineEnd);
        request.writeBytes(lineEnd);
        request.write(xml.toString().getBytes());
        request.writeBytes(lineEnd + lineEnd);
        request.writeBytes(twoHyphens + boundary + lineEnd);

        if (csrf != null) {
            request.writeBytes("Content-Disposition: form-data; name=\"X-CSRF-TOKEN\"" + lineEnd);
            request.writeBytes(lineEnd);
            request.writeBytes(csrf);
            request.writeBytes(lineEnd);
            request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        } else {
            logger.warn("The CSRF is null, trying the dashboard upload without the CSRF token");
        }

        request.flush();
        request.close();
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
            int status = connection.getResponseCode();
            if (status == 200) {
                logger.info("Successfully Imported the dashboard {}", fileName);
            } else {
                logger.error("The server responded with a status of {}", status);
                String content = null;
                if (inputStream != null) {
                    content = IOUtils.toString(inputStream);
                }
                logger.error("The response headers are {} and content is [{}]",
                        connection.getHeaderFields(), content);
            }

        } catch (IOException e) {
            logger.error("Error while uploading the dashboard " + urlStr, e);
            InputStream errorStream = connection.getErrorStream();
            String content = null;
            if (errorStream != null) {
                content = IOUtils.toString(errorStream);
            }
            logger.error("The error response headers are {} and content is [{}]",
                    connection.getHeaderFields(), content);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String getTextValue(JsonNode node) {
        if (node != null) {
            return node.getTextValue();
        }
        return null;
    }

    public static SSLSocketFactory createSSLSocketFactory() {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
            }, new SecureRandom());
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CustomHostnameVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
