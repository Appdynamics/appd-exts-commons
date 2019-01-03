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

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.CookiesCsrf;
import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
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
import static com.appdynamics.extensions.Constants.URI;
import static com.appdynamics.extensions.dashboard.DashboardConstants.APPLICATION_JSON;
import static com.appdynamics.extensions.dashboard.DashboardConstants.JSON;
import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUploader.class);
    private CustomDashboardAPIService customDashboardAPIService;

    public CustomDashboardUploader(CustomDashboardAPIService customDashboardAPIService) {
        this.customDashboardAPIService = customDashboardAPIService;
    }

    public void checkAndUpload(String dashboardName, String fileContents, Map<String, ?> proxyMap, boolean overwrite) throws ControllerHttpRequestException {
        JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
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
                customDashboardAPIService.uploadDashboard(proxyMap, dashboardName, fileExtension, fileContents, fileContentType);
            } else {
                logger.debug("Overwrite Disabled, not attempting to overwrite dashboard: {}", dashboardName);
            }
        } else {
            customDashboardAPIService.uploadDashboard(proxyMap, dashboardName, fileExtension, fileContents, fileContentType);
        }
    }

    //#TODO If all getAlldashboards returns null, dashboard is considered not present, a potential for dashboards explosion.
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
}
