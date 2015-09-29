package com.appdynamics.extensions.dashboard;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.StringUtils;
import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.http.SimpleHttpClientBuilder;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.xml.Xml;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {
    public static final Logger logger = LoggerFactory.getLogger(CustomDashboardUploader.class);

    public void uploadDashboard(String dashboardName, Xml xml, Map<String, String> argsMap, boolean overwrite) {
        setProxyIfApplicable(argsMap);
        SimpleHttpClient client = new SimpleHttpClientBuilder(argsMap).connectionTimeout(2000).socketTimeout(2000).build();
        try {
            Response response = client.target().path("controller/auth?action=login").get();
            if (response.getStatus() == 200) {
                Header[] headers = response.getHeaders();
                StringBuilder cookies = new StringBuilder();
                String csrf = null;
                for (Header header : headers) {
                    if (header.getName().equalsIgnoreCase("set-cookie")) {
                        String value = header.getValue();
                        cookies.append(value).append(";");
                        if (value.toLowerCase().contains("x-csrf-token")) {
                            csrf = value.split("=")[1];
                        }
                    }
                }
                logger.debug("The controller login is successful, the cookie is [{}] and csrf is {}", cookies, csrf);
                boolean isPresent = isDashboardPresent(client, cookies, dashboardName);
                if (isPresent) {
                    if (overwrite) {
                        uploadFile(dashboardName, xml, argsMap, cookies, csrf);
                    } else {
                        logger.debug("The dashboard {} exists or API has been changed, not processing dashboard upload", dashboardName);
                    }
                } else {
                    uploadFile(dashboardName, xml, argsMap, cookies, csrf);
                }
            } else {
                logger.error("Custom Dashboard Upload Failed. The login to the controller is unsuccessful. The response code is {}"
                        , response.getStatus());
                logger.error("The response headers are {} and content is {}", Arrays.toString(response.getHeaders()), response.string());
            }
        } finally {
            client.close();
        }
    }

    private void setProxyIfApplicable(Map<String, String> argsMap) {
        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");
        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {
            argsMap.put(TaskInputArgs.PROXY_HOST, proxyHost);
            argsMap.put(TaskInputArgs.PROXY_PORT, proxyPort);
            logger.debug("Using the proxy {}:{} to upload the dashboard", proxyHost, proxyPort);
        } else {
            logger.debug("Not using proxy for dashboard upload appdynamics.http.proxyHost={} and appdynamics.http.proxyPort={}"
                    , proxyHost, proxyPort);
        }
    }

    private boolean isDashboardPresent(SimpleHttpClient client, StringBuilder cookies, String dashboardName) {
        Response response = client.target().path("controller/restui/dashboards/list/false")
                .header("Cookie", cookies.toString()).get();
        if (response.getStatus() == 200) {
            ArrayNode arrayNode = response.json(ArrayNode.class);
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
        } else {
            logger.error("The controller API [isDashboardPresent] returned invalid response{}, so cannot upload the dashboard"
                    , response.getStatus());
            logger.info("Please change the [uploadDashboard] property in the config.yml to false. " +
                    "The xml will be written to the logs folder. Please import it to controller manually");
            return false;
        }
    }

    private void uploadFile(String instanceName, Xml xml, Map<String, String> argsMap, StringBuilder cookies, String csrf) {
        try {
            uploadFile(instanceName, xml, cookies, argsMap, csrf);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void uploadFile(String dashboardName, Xml xml, StringBuilder cookies, Map<String, String> argsMap, String csrf) throws IOException {
        String fileName = dashboardName + ".xml";
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";

        String urlStr = new UrlBuilder(argsMap).path("controller/CustomDashboardImportExportServlet").build();
        logger.info("Uploading the custom Dashboard {} to {}", dashboardName, urlStr);

        HttpURLConnection connection = null;
        URL url = new URL(urlStr);
        if (argsMap.containsKey(TaskInputArgs.PROXY_HOST)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(argsMap.get(TaskInputArgs.PROXY_HOST)
                    , Integer.parseInt(argsMap.get(TaskInputArgs.PROXY_PORT))));
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
        InputStream inputStream = connection.getInputStream();
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
        if (inputStream != null) {
            inputStream.close();
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
