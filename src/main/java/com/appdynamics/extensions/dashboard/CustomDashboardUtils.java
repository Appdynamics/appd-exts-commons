/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 9/18/18.
 */
public class CustomDashboardUtils {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUtils.class);

    public static String getDashboardName(Map dashboardConfig) {
        String dashboardName;
        if (!Strings.isNullOrEmpty((String) dashboardConfig.get("dashboardName"))) {
            dashboardName = dashboardConfig.get("dashboardName").toString();
        } else {
            dashboardName = "Custom Dashboard";
        }
        dashboardConfig.put("dashboardName", dashboardName);
        return dashboardName;
    }

    public static boolean getOverwrite(Map customDashboardConfig) {
        boolean overwrite;
        if (customDashboardConfig.get("overwriteDashboard") != null) {
            overwrite = (Boolean) customDashboardConfig.get("overwriteDashboard");
        } else {
            overwrite = false;
        }
        return overwrite;
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
            dashboardMetricPrefix.append(metricPrefix);
        }
        logger.debug("Dashboard Metric Prefix = " + dashboardMetricPrefix.toString());
        return dashboardMetricPrefix.toString();
    }
}
