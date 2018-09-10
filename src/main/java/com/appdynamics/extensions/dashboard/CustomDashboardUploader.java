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

import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.api.CookiesCsrf;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;

import java.util.Map;

import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUploader.class);

    private ControllerApiService apiService;

    public CustomDashboardUploader(ControllerApiService apiService) {
        this.apiService = apiService;
    }

    public void uploadDashboard(CloseableHttpClient client, String dashboardName, String fileExtension, String fileContents, String contentType, Map<String, ? super Object> argsMap, boolean overwrite) throws ApiException {
        try {

            CookiesCsrf cookiesCsrf = apiService.getCookiesAndAuthToken(client);
            JsonNode arrayNode = apiService.getAllDashboards(client, cookiesCsrf);

            boolean isPresent = isDashboardPresent(dashboardName, arrayNode);
            logger.debug("Dashboard present: {}", isPresent);
            logger.debug("Dashboard overwrite: {}", overwrite);
            if (isPresent) {
                if (overwrite) {
                    //#NOTE Even though we intend to overwrite, this will actually create a new dashboard.
                    // This will not be present in the config.yml so it will never override.
                    // Keeping this here for when override will be supported
                    apiService.uploadDashboard(argsMap, cookiesCsrf, dashboardName, fileExtension, fileContents, contentType);
                } else {
                    logger.debug("Dashboard {} Already present, can not overwrite. ", dashboardName);
                }
            } else {
                apiService.uploadDashboard(argsMap, cookiesCsrf, dashboardName, fileExtension, fileContents, contentType);
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

}
