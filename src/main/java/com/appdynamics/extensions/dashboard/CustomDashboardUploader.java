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
import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.api.CookiesCsrf;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;
import static com.appdynamics.extensions.dashboard.DashboardConstants.AT;
import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUploader.class);

    private ControllerApiService apiService;
    private ControllerInfo controllerInfo;

    public CustomDashboardUploader(ControllerApiService apiService, ControllerInfo controllerInfo) {
        this.apiService = apiService;
        this.controllerInfo = controllerInfo;
    }

    //TODO create the client here instead of module
    public void uploadDashboard( String dashboardName,  String fileContents,  Map<String, ? > dashboardConfig,   boolean overwrite) throws ApiException {
        Map httpProperties = httpProperties(dashboardConfig, controllerInfo);
        CloseableHttpClient client = null;

        try {
            setProxyIfApplicable(httpProperties);
            client = Http4ClientBuilder.getBuilder(httpProperties).build();
            CookiesCsrf cookiesCsrf = apiService.getCookiesAndAuthToken(client);
            JsonNode arrayNode = apiService.getAllDashboards(client, cookiesCsrf);

            String fileExtension = "json";
            String contentType = "application/json";


            boolean isPresent = isDashboardPresent(dashboardName, arrayNode);
            logger.debug("Dashboard present: {}", isPresent);
            logger.debug("Dashboard overwrite: {}", overwrite);
            if (isPresent) {
                if (overwrite) {
                    //#NOTE Even though we intend to overwrite, this will actually create a new dashboard.
                    // This will not be present in the config.yml so it will never override.
                    // Keeping this here for when override will be supported
                    apiService.uploadDashboard(httpProperties, cookiesCsrf, dashboardName, fileExtension, fileContents, contentType);
                } else {
                    logger.debug("Dashboard {} Already present, can not overwrite. ", dashboardName);
                }
            } else {
                apiService.uploadDashboard(httpProperties, cookiesCsrf, dashboardName, fileExtension, fileContents, contentType);
            }
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private boolean isDashboardPresent(String dashboardName, JsonNode arrayNode) {
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
    }


    protected Map<String, ? super Object> httpProperties(Map customDashboardConfig, ControllerInfo controllerInfo) {

        Map<String, ? super Object> httpProperties = new HashMap<>();

        List<Map<String, ? super Object>> serverList = Lists.newArrayList();
        Map<String, ? super Object> serverMap = getServerMap(controllerInfo);
        serverList.add(serverMap);
        httpProperties.put("servers", serverList);

        Map<String, ? super Object> connectionMap = getConnectionMap(customDashboardConfig);
        httpProperties.put("connection", connectionMap);

        return httpProperties;
    }

    private Map<String, ? super Object> getServerMap(ControllerInfo controllerInfo) {
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, controllerInfo.getControllerHost());
        serverMap.put(TaskInputArgs.PORT, String.valueOf(controllerInfo.getControllerPort()));
        serverMap.put(TaskInputArgs.USE_SSL, String.valueOf(controllerInfo.getControllerSslEnabled()));
        serverMap.put(TaskInputArgs.USER, getUserName(controllerInfo));
        serverMap.put(TaskInputArgs.PASSWORD, controllerInfo.getPassword());

        logger.debug("Controller Info: ");
        logger.debug(TaskInputArgs.HOST + ": {}", controllerInfo.getControllerHost());
        logger.debug(TaskInputArgs.PORT + ": {}", String.valueOf(controllerInfo.getControllerPort()));
        logger.debug(TaskInputArgs.USE_SSL + ": {}", controllerInfo.getControllerSslEnabled());
        logger.debug(TaskInputArgs.USER + ": {}", getUserName(controllerInfo));
        return serverMap;
    }

    private Map<String, ? super Object> getConnectionMap(Map customDashboardConfig) {
        Map<String, ? super Object> connectionMap = new HashMap<>();
        String[] sslProtocols = {TLSV_12};
        connectionMap.put(TaskInputArgs.SSL_PROTOCOL, sslProtocols);
        Object sslCertCheckEnabled = customDashboardConfig.get(SSL_CERT_CHECK_ENABLED);
        if (sslCertCheckEnabled != null) {
            connectionMap.put(SSL_CERT_CHECK_ENABLED, Boolean.valueOf(sslCertCheckEnabled.toString()));
        } else {
            connectionMap.put(SSL_CERT_CHECK_ENABLED, true);
        }
        connectionMap.put(CONNECT_TIMEOUT, 10000);
        connectionMap.put(SOCKET_TIMEOUT, 15000);
        return connectionMap;
    }

    private String getUserName(ControllerInfo controllerInfo) {
        String accountName = controllerInfo.getAccount();
        String username = controllerInfo.getUsername();
        if (accountName != null && username != null) {
            return username + AT + accountName;
        }
        return "";
    }

    // todo add proxy support from config
    private void setProxyIfApplicable(Map<String, ? super Object> argsMap) {
        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");
        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {
            Map<String, ? super Object> proxyMap = new HashMap<>();
            proxyMap.put(TaskInputArgs.HOST, proxyHost);
            proxyMap.put(TaskInputArgs.PORT, proxyPort);
            argsMap.put("proxy", proxyMap);
            logger.debug("Using the proxy {}:{} to upload the dashboard", proxyHost, proxyPort);
        }
        else {
            logger.debug("Not using proxy for dashboard upload appdynamics.http.proxyHost={} and appdynamics.http.proxyPort={}"
                    , proxyHost, proxyPort);
        }
    }

}
