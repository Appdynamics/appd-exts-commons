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
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUploader.class);

    private ControllerApiService apiService = new ControllerApiService();

    public void uploadDashboard(String dashboardName, String fileExtension, String fileContents, String contentType, Map<String, ? super Object> argsMap, boolean overwrite) throws ApiException {
        setProxyIfApplicable(argsMap);
        CloseableHttpClient client = null;
        try {

            // TODO create a map of server info here and you dont need to do that in the generator class
            // TODO the creation of the client can be moved in the initCustomDashboard in the module class
            // TODO pass controllerInformation -> serversMap -> get this information from the module
            // TODO replace serverstringmap by controllerInfo object in api service
            client = Http4ClientBuilder.getBuilder(argsMap).build();
            List<Map<String, ?>> serversList = (List<Map<String, ?>>) argsMap.get("servers");
            Map<String, String> serverStringMap = (Map<String, String>) serversList.iterator().next();
//            Map<String, String> serverStringMap = new HashMap<>();
//            serverStringMap.put(TaskInputArgs.HOST, (String) serverMap.get(TaskInputArgs.HOST));
//            serverStringMap.put(TaskInputArgs.PORT, (String) serverMap.get(TaskInputArgs.PORT));
//            serverStringMap.put(TaskInputArgs.USE_SSL, String.valueOf(serverMap.get(TaskInputArgs.USE_SSL)));
            CookiesCsrf cookiesCsrf = apiService.getCookiesAndAuthToken(client, serverStringMap);
            JsonNode arrayNode = apiService.getAllDashboards(client, serverStringMap, cookiesCsrf);

            boolean isPresent = isDashboardPresent(dashboardName, arrayNode);
            logger.debug("Dashboard present: {}", isPresent);
            logger.debug("Dashboard overwrite: {}", overwrite);
            if (isPresent) {
                if (overwrite) {
                    //#TODO Eventhough we intend to overwrite, this will actually create a new dashboard.
                    // This will not be present in the config.yml so it will never override.
                    // Keeping this here for when override will be supported
                    apiService.uploadDashboard(serverStringMap,argsMap,cookiesCsrf,dashboardName,fileExtension,fileContents,contentType);
                } else {
                    logger.debug("Dashboard {} Already present, can not overwrite. ", dashboardName);
                }
            } else {
                apiService.uploadDashboard(serverStringMap, argsMap, cookiesCsrf, dashboardName, fileExtension, fileContents, contentType);
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

    // TODO this will go with where we create the http client
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

}
