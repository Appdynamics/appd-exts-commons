/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.Map;

import static com.appdynamics.extensions.Constants.ENABLED;
import static com.appdynamics.extensions.dashboard.DashboardConstants.*;

/**
 * Created by bhuvnesh.kumar on 9/18/18.
 */
public class CustomDashboardUtils {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUtils.class);

    public static String getDashboardName(Map customDashboardConfig, String monitorName) {
        String dashboardName;
        if (!Strings.isNullOrEmpty((String) customDashboardConfig.get(DASHBOARD_NAME))) {
            dashboardName = customDashboardConfig.get(DASHBOARD_NAME).toString();
        } else {
            dashboardName = monitorName + " Dashboard";
        }
        return dashboardName;
    }

    public static boolean getOverwrite(Map customDashboardConfig) {
        if (customDashboardConfig.get(OVERWRITE_DASHBOARD) != null) {
            return (Boolean) customDashboardConfig.get(OVERWRITE_DASHBOARD);
        }
        return false;
    }

    public static String buildMetricPrefixForDashboard(String metricPrefix) {
        StringBuilder dashboardMetricPrefix = new StringBuilder();
        if (metricPrefix.contains("Server")) {
            String[] metricPath = metricPrefix.split("\\|");
            int count = 0;
            for (String str : metricPath) {
                count++;
                if (count > 2) {
                    dashboardMetricPrefix.append(str).append("|");
                }
            }
            dashboardMetricPrefix.deleteCharAt(dashboardMetricPrefix.length() - 1);
        } else {
            if (metricPrefix.length() > 0 && metricPrefix.charAt(metricPrefix.length() - 1) == '|') {
                metricPrefix = metricPrefix.substring(0, metricPrefix.length() - 1);
            }
            dashboardMetricPrefix.append(metricPrefix);
        }
        logger.debug("Dashboard Metric Prefix = " + dashboardMetricPrefix.toString());
        return dashboardMetricPrefix.toString();
    }

    public static boolean isCustomDashboardEnabled(Map customDashboardConfig) {
        return customDashboardConfig != null && !customDashboardConfig.isEmpty() &&
                (Boolean) customDashboardConfig.get(ENABLED);
    }

    public static int getTimeDelay(Map customDashboardConfig) {
        Integer num = (Integer) customDashboardConfig.get("periodicDashboardCheckInSeconds");
        if (num != null) {
            return num;
        }
        return DEFAULT_PERIODIC_DASHBOARD_CHECK_IN_SECONDS;
    }

    public static String getDashboardTemplate(String metricPrefix, Map customDashboardConfig, ControllerInfo controllerInfo, String dashboardName) {
            String dashboardMetricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metricPrefix);
            CustomDashboardTemplateGenerator templateGenerator = new CustomDashboardTemplateGenerator(customDashboardConfig, controllerInfo,
                    dashboardMetricPrefix, dashboardName);
            return templateGenerator.getDashboardTemplate();
    }

    public static Boolean isValidDashboardTemplate(String dashboardTemplate) {
        if (!Strings.isNullOrEmpty(dashboardTemplate)) {
            logger.debug("Dashboard values resolved. Ready for uploader");
            return true;
        }
        logger.debug("Dashboard is not initialized, skipping upload.");
        return false;
    }
}
