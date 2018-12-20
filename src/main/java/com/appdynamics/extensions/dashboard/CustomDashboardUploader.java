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

import com.appdynamics.extensions.Constants;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.CookiesCsrf;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
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
import java.util.Map;

import static com.appdynamics.extensions.Constants.HOST;
import static com.appdynamics.extensions.Constants.PORT;
import static com.appdynamics.extensions.dashboard.DashboardConstants.APPLICATION_JSON;
import static com.appdynamics.extensions.dashboard.DashboardConstants.JSON;
import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUploader.class);
    private ControllerInfo controllerInfo;
    private ControllerClient controllerClient;

    //#TODO Check if ControllerInfo is required
    public CustomDashboardUploader(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        this.controllerInfo = controllerInfo;
        this.controllerClient = controllerClient;
    }

    public void checkAndUpload(String dashboardName, String fileContents, Map<String, ?> proxyMap, boolean overwrite) throws ControllerHttpRequestException {
        JsonNode allDashboardsNode = getAllDashboards();
        // #TODO Check if cookiesCsrf from a different HttpClient can be used.
        CookiesCsrf cookiesCsrf = controllerClient.getCookiesCsrf();
        String fileExtension = JSON;
        String fileContentType = APPLICATION_JSON;
        if (isDashboardPresent(dashboardName, allDashboardsNode)) {
            if (overwrite) {
                logger.debug("Overwriting dashboard: {}", dashboardName);
                /** Even though we intend to overwrite, this will actually create a new dashboard.
                 * This option will not be exposed in the config.yml, which means it will always be false and
                 * so we never try to overwrite.
                 * This option needs to be expose when the editing option is supported through controller APIs.
                 * */
                uploadDashboard(proxyMap, cookiesCsrf, dashboardName, fileExtension, fileContents, fileContentType);
            } else {
                logger.debug("Overwrite Disabled, not attempting to overwrite dashboard: {}", dashboardName);
            }
        } else {
            uploadDashboard(proxyMap, cookiesCsrf, dashboardName, fileExtension, fileContents, fileContentType);
        }
    }

    // #TODO If all getAlldashboards returns null, dashboard is considered not present, a potential for dashboards explosion.
    private JsonNode getAllDashboards() {
        JsonNode allDashboardsNode = null;
        String alldashboards = null;
        try {
            alldashboards = controllerClient.sendGetRequest("controller/restui/dashboards/getAllDashboardsByType/false");
            return allDashboardsNode = new ObjectMapper().readTree(alldashboards);
        } catch (ControllerHttpRequestException e) {
            logger.error("Invalid response from controller while fetching information about all dashbards", e);
        } catch (IOException e) {
            logger.error("Error while getting all dashboards information", e);
        }
        return allDashboardsNode;
    }

    private boolean isDashboardPresent(String dashboardName, JsonNode existingDashboards) {
        if (existingDashboards != null) {
            for (JsonNode existingDashboard : existingDashboards) {
                if (dashboardName.equals(getTextValue(existingDashboard.get("name")))) {
                    logger.debug("Dashboard Already present: {}", dashboardName);
                    return true;
                }
            }
        }
        logger.debug("Dashboard not present, attempting to upload: {} ", dashboardName);
        return false;
    }

    public void uploadDashboard(Map<String, ?> proxyMap, CookiesCsrf cookiesCsrf, String dashboardName, String fileExtension, String fileContent, String fileContentType) throws ControllerHttpRequestException {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.host(controllerInfo.getControllerHost());
        urlBuilder.port(controllerInfo.getControllerPort());
        urlBuilder.ssl(controllerInfo.getControllerSslEnabled());

        String filename = dashboardName + "." + fileExtension;
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";
        String urlStr = urlBuilder.path("controller/CustomDashboardImportExportServlet").build();
        logger.info("Uploading the custom Dashboard {} to {}", filename, urlStr);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            if (proxyMap != null && !proxyMap.isEmpty()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((String) proxyMap.get(HOST)
                        , Integer.parseInt((String) proxyMap.get(PORT))));
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
            connection.setRequestProperty("Cookie", cookiesCsrf.getCookies());

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.writeBytes(twoHyphens + boundary + lineEnd);
            request.writeBytes("Content-Disposition: form-data; name=\"" + dashboardName + "\";filename=\"" + filename + "\"" + lineEnd);
            request.writeBytes("Content-Type: " + fileContentType + lineEnd);
            request.writeBytes(lineEnd);
            request.write(fileContent.getBytes());
            request.writeBytes(lineEnd + lineEnd);
            request.writeBytes(twoHyphens + boundary + lineEnd);

            if (cookiesCsrf.getCsrf() != null) {
                request.writeBytes("Content-Disposition: form-data; name=\"X-CSRF-TOKEN\"" + lineEnd);
                request.writeBytes(lineEnd);
                request.writeBytes(cookiesCsrf.getCsrf());
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
                    logger.info("Successfully Imported the dashboard {}", filename);
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
                throw new ControllerHttpRequestException("Error while uploading the dashboard", e);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            throw new ControllerHttpRequestException("Error in uploading dashboard", e);
        }

    }

    private static SSLSocketFactory createSSLSocketFactory() throws ControllerHttpRequestException {
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
            throw new ControllerHttpRequestException("Unsupported algorithm", e);
        } catch (KeyManagementException e) {
            throw new ControllerHttpRequestException("Key Management exception", e);
        }
    }

    private static class CustomHostnameVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
