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

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;

/**
 * Created by abey.tom on 4/10/15.
 */
public class CustomDashboardTemplateGenerator {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardTemplateGenerator.class);
    private String metricPrefix;
    private Map customDashboardConfig;
    private ControllerInfo controllerInfo;
    private String dashboardName;

    public CustomDashboardTemplateGenerator(Map customDashboardConfig, ControllerInfo controllerInformation, String metricPrefix, String dashboardName) {
        this.customDashboardConfig = customDashboardConfig;
        this.controllerInfo = controllerInformation;
        this.metricPrefix = metricPrefix;
        this.dashboardName = dashboardName;
    }

    public String getDashboardTemplate() {
        String dashboardTemplate = getDashboardContents();
        dashboardTemplate = replaceDefaultDashboardInfo(dashboardTemplate);
        return dashboardTemplate;
    }

    private String getDashboardContents() {
        logger.debug("Sim Enabled: {}", controllerInfo.getSimEnabled());
        String dashboardTemplate = "";
        String pathToFile;
        if (!controllerInfo.getSimEnabled()) {
            pathToFile = customDashboardConfig.get("pathToNormalDashboard").toString();
        } else {
            pathToFile = customDashboardConfig.get("pathToSIMDashboard").toString();
        }
        try {
            if (!Strings.isNullOrEmpty(pathToFile)) {
                File file = new File(pathToFile);
                if (file.exists()) {
                    dashboardTemplate = FileUtils.readFileToString(file);
                } else {
                    logger.error("Unable to read the contents of the dashboard file: {}", pathToFile);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read the contents of the dashboard file: {}", e);
        }
        return dashboardTemplate;
    }

    private String replaceDefaultDashboardInfo(String dashboardString) {
        dashboardString = setMetricPrefix(dashboardString);
        dashboardString = setApplicationName(dashboardString);
        dashboardString = setSimApplicationName(dashboardString);
        dashboardString = setTierName(dashboardString);
        dashboardString = setNodeName(dashboardString);
        dashboardString = setHostName(dashboardString);
        dashboardString = setDashboardName(dashboardString);
        dashboardString = setMachinePath(dashboardString);
        return dashboardString;
    }

    private String setMetricPrefix(String dashboardString) {
        if (dashboardString.contains(REPLACE_METRIC_PREFIX)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_METRIC_PREFIX, metricPrefix);
            logger.debug(REPLACE_METRIC_PREFIX + ": " + metricPrefix);
        }
        return dashboardString;
    }

    private String setApplicationName(String dashboardString) {
        if (dashboardString.contains(REPLACE_APPLICATION_NAME)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_APPLICATION_NAME, controllerInfo.getApplicationName());
            logger.debug(REPLACE_APPLICATION_NAME + ": " + controllerInfo.getApplicationName());
        }
        return dashboardString;
    }

    private String setSimApplicationName(String dashboardString) {
        if (dashboardString.contains(REPLACE_SIM_APPLICATION_NAME)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_SIM_APPLICATION_NAME, SIM_APPLICATION_NAME);
            logger.debug(REPLACE_SIM_APPLICATION_NAME + ": " + SIM_APPLICATION_NAME);
        }
        return dashboardString;
    }

    private String setTierName(String dashboardString) {
        if (dashboardString.contains(REPLACE_TIER_NAME)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_TIER_NAME, controllerInfo.getTierName());
            logger.debug(REPLACE_TIER_NAME + ": " + controllerInfo.getTierName());
        }
        return dashboardString;
    }

    private String setNodeName(String dashboardString) {
        if (dashboardString.contains(REPLACE_NODE_NAME)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_NODE_NAME, controllerInfo.getNodeName());
            logger.debug(REPLACE_NODE_NAME + ": " + controllerInfo.getNodeName());
        }
        return dashboardString;
    }

    private String setHostName(String dashboardString) {
        if (dashboardString.contains(REPLACE_HOST_NAME)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_HOST_NAME, controllerInfo.getControllerHost());
            logger.debug(REPLACE_HOST_NAME + ": " + controllerInfo.getControllerHost());
        }
        return dashboardString;
    }

    private String setDashboardName(String dashboardString) {
        if (dashboardString.contains(REPLACE_DASHBOARD_NAME)) {
            dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_DASHBOARD_NAME, dashboardName);
            logger.debug(REPLACE_DASHBOARD_NAME + ": " + dashboardName);
        }
        return dashboardString;
    }

    private String setMachinePath(String dashboardString) {
        if (dashboardString.contains(REPLACE_MACHINE_PATH)) {
            if (controllerInfo.getMachinePath() != null) {
                String machinePath = ROOT + METRICS_SEPARATOR + controllerInfo.getMachinePath();
                machinePath = machinePath.substring(0, machinePath.lastIndexOf(METRICS_SEPARATOR));
                dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_MACHINE_PATH, machinePath);
                logger.debug(REPLACE_MACHINE_PATH + ": " + machinePath);
            } else {
                dashboardString = org.apache.commons.lang3.StringUtils.replace(dashboardString, REPLACE_MACHINE_PATH, ROOT);
                logger.debug(REPLACE_MACHINE_PATH + ": " + ROOT);
            }
        }
        return dashboardString;
    }

}
