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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.CustomDashboardUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.appdynamics.extensions.dashboard.DashboardConstants.CUSTOM_DASHBOARD;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);
    private boolean initialized;
    private String dashboardName;
    private boolean overwrite;
    private String dashboardTemplate;
    private CustomDashboardUploader dashboardUploader;
    private long timeDelayInMilliSeconds;
    private volatile AtomicLong lastRecordedTime;
    private Map<String, ?> config;

    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, String monitorName, ControllerInfo controllerInfo, ControllerAPIService controllerAPIService) {
        initialized = false;
        lastRecordedTime = new AtomicLong();
        if(controllerInfo == null || controllerAPIService == null) {
            logger.debug("ControllerInfo/ControllerClient is null.....Not initializing CustomDashBoardModule");
            return;
        }
        this.config = config;
        Map customDashboardConfig = (Map) config.get(CUSTOM_DASHBOARD);
        if (CustomDashboardUtils.isCustomDashboardEnabled(customDashboardConfig)) {
            dashboardName = CustomDashboardUtils.getDashboardName(customDashboardConfig, monitorName);
            String dashboardTemplate = CustomDashboardUtils.getDashboardTemplate(metricPrefix, customDashboardConfig, controllerInfo, dashboardName);
            if (CustomDashboardUtils.isValidDashboardTemplate(dashboardTemplate)) {
                this.dashboardTemplate = dashboardTemplate;
                overwrite = CustomDashboardUtils.getOverwrite(customDashboardConfig);
                timeDelayInMilliSeconds = CustomDashboardUtils.getTimeDelay(customDashboardConfig) * 1000;
                dashboardUploader = new CustomDashboardUploader(controllerAPIService.getCustomDashboardAPIService());
                initialized = true;
            }
        } else {
            logger.info("Custom Dashboard is not enabled in config.yml.");
        }
    }

    public synchronized void uploadDashboard() {
        if (initialized) {
            long currentTime = System.currentTimeMillis();
            if (hasTimeElapsed(currentTime, lastRecordedTime.get(), timeDelayInMilliSeconds)) {
                try {
                    logger.debug("Attempting to upload dashboard: {}", dashboardName);
                    dashboardUploader.checkAndUpload(dashboardName, dashboardTemplate, config, overwrite);
                    lastRecordedTime.set(currentTime);
                    long endTime = System.currentTimeMillis();
                    logger.debug("Time to complete customDashboardModule  :" + (endTime - currentTime) + " ms");
                } catch (ControllerHttpRequestException e) {
                    logger.error("Error while checking and uploading dashboard", e);
                } catch (Exception e) {
                    logger.error("Unable to establish connection, not uploading dashboard.", e);
                }
            }
        } else {
            logger.debug("Auto upload of custom dashboard is disabled.");
        }
    }

    private boolean hasTimeElapsed(long curr, long prev, long threshold) {
        return (curr - prev > threshold);
    }
}