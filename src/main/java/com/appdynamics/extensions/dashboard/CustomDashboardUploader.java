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

    public void gatherDashboardDataToUpload(ControllerApiService apiService, CloseableHttpClient client, String dashboardName,
                                            String fileContents, Map httpProperties, boolean overwrite) throws ApiException {
        try {
            CookiesCsrf cookiesCsrf = apiService.getCookiesAndAuthToken(client);
            JsonNode allDashboards = apiService.getAllDashboards(client, cookiesCsrf);
            uploadDashboard(apiService, dashboardName, fileContents, overwrite, httpProperties, cookiesCsrf, allDashboards);
        } catch (Exception e) {
            logger.error("Unable to send dashboard", e);
        }
    }

    private void uploadDashboard(ControllerApiService apiService, String dashboardName, String fileContents, boolean overwrite,
                                 Map httpProperties, CookiesCsrf cookiesCsrf, JsonNode allDashboards) throws ApiException {
        String fileExtension = "json";
        String fileContentType = "application/json";
        logger.debug("Dashboard overwrite: {}", overwrite);
        if (isDashboardPresent(dashboardName, allDashboards)) {
            if (overwrite) {
                //#NOTE Even though we intend to overwrite, this will actually create a new dashboard.
                // This will not be present in the config.yml so it will never override.
                // Keeping this here for when override will be supported
                apiService.uploadDashboard(httpProperties, cookiesCsrf, dashboardName, fileExtension, fileContents, fileContentType);
            } else {
                logger.debug("Dashboard {} Already present, can not overwrite. ", dashboardName);
            }
        } else {
            apiService.uploadDashboard(httpProperties, cookiesCsrf, dashboardName, fileExtension, fileContents, fileContentType);
        }
    }

    private boolean isDashboardPresent(String dashboardName, JsonNode existingDashboards) {
        if (existingDashboards != null) {
            for (JsonNode existingDashboard : existingDashboards) {
                if (dashboardName.equals(getTextValue(existingDashboard.get("name")))) {
                    logger.debug("Dashboard present: {}", true);
                    return true;
                }
            }
        }
        logger.debug("Dashboard present: {}", false);
        return false;
    }
}
