/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.CookiesCsrf;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import static com.appdynamics.extensions.Constants.URI;
import static com.appdynamics.extensions.util.SSLUtils.createHostNameVerifier;
import static com.appdynamics.extensions.util.SSLUtils.createSSLContext;

/**
 * Created by venkata.konala on 1/1/19.
 */
public class CustomDashboardAPIService extends APIService{

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardAPIService.class);

    CustomDashboardAPIService(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        super(controllerInfo, controllerClient);
    }

    public JsonNode getAllDashboards() {
        if(controllerClient != null) {
            JsonNode allDashboardsNode = null;
            String alldashboards;
            try {
                alldashboards = controllerClient.sendGetRequest("controller/restui/dashboards/getAllDashboardsByType/false");
                allDashboardsNode = new ObjectMapper().readTree(alldashboards);
            } catch (ControllerHttpRequestException e) {
                logger.error("Invalid response from controller while fetching information about all dashbards", e);
            } catch (IOException e) {
                logger.error("Error while getting all dashboards information", e);
            }
            return allDashboardsNode;
        }
        logger.debug("The controllerClient is not initialized");
        return null;
    }

    // #TODO Check if cookiesCsrf from a different HttpClient can be used.
    public void uploadDashboard(Map<String, ?> propMap, String dashboardName, String fileExtension, String fileContent, String fileContentType) throws ControllerHttpRequestException {
        Map<String, ?> connectionMap = (Map<String, ?>)propMap.get("connection");
        Map<String, ?> proxyMap = (Map<String, ?>)propMap.get("proxy");

        CookiesCsrf cookiesCsrf = controllerClient.getCookiesCsrf();
        String filename = dashboardName + "." + fileExtension;
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";
        String urlStr = controllerClient.getBaseURL() + "controller/CustomDashboardImportExportServlet";
        logger.info("Uploading the custom Dashboard {} to {}", filename, urlStr);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            if (proxyMap != null && !proxyMap.isEmpty() && !Strings.isNullOrEmpty((String)proxyMap.get(URI))) {
                URL proxyURL = new URL((String)proxyMap.get(URI));
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyURL.getHost()
                        , proxyURL.getPort()));
                connection = (HttpURLConnection) url.openConnection(proxy);
                logger.debug("Created an HttpConnection for Fileupload with a proxy {}", proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                httpsURLConnection.setSSLSocketFactory(createSSLContext(PathResolver.resolveDirectory(ABaseMonitor.class), propMap).getSocketFactory());
                httpsURLConnection.setHostnameVerifier(createHostNameVerifier(connectionMap));
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
        } catch (Exception e) {
            throw new ControllerHttpRequestException("Error in uploading dashboard", e);
        }

    }

    /*#TODO The SSLContext creation needs to be a common utility available fot all clients.*/

}
