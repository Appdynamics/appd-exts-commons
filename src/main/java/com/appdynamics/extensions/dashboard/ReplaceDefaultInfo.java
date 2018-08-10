/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;

//import static com.appdynamics.extensions.aws.elb.Constants.*;

/**
 * Created by bhuvnesh.kumar on 8/7/18.
 */
public class ReplaceDefaultInfo {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(ReplaceDefaultInfo.class);


    public static String replaceFields(String dashboardString, ControllerInfo controllerInfo, Map config) {

        dashboardString = replaceApplicationName(dashboardString, controllerInfo);
        dashboardString = replaceTierName(dashboardString, controllerInfo);
        dashboardString = replaceNodeName(dashboardString, controllerInfo);
        dashboardString = replaceDashboardName(dashboardString, controllerInfo, config);
        dashboardString = replaceSimApplicationName(dashboardString, controllerInfo);
        dashboardString = replaceHostName(dashboardString, controllerInfo);
        dashboardString = replaceMachinePath(dashboardString, controllerInfo);

        return dashboardString;

    }

    private static String replaceHostName(String dashboardString, ControllerInfo controllerInfo) {
        if (dashboardString.contains(REPLACE_HOST_NAME)) {
            if (controllerInfo.getControllerHost() != null) {
                LOGGER.debug("replacing Host Name: {}", controllerInfo.getControllerHost());
                dashboardString = dashboardString.replace(REPLACE_HOST_NAME, controllerInfo.getControllerHost());
            }
        }
        return dashboardString;
    }

    private static String replaceSimApplicationName(String dashboardString, ControllerInfo controllerInfo) {
        if (dashboardString.contains(REPLACE_SIM_APPLICATION_NAME)) {
            LOGGER.debug("replacing SimApplicationName: {}", SIM_APPLICATION_NAME);
            dashboardString = dashboardString.replace(REPLACE_SIM_APPLICATION_NAME, SIM_APPLICATION_NAME);
        }
        return dashboardString;

    }

    private static String replaceDashboardName(String dashboardString, ControllerInfo controllerInfo, Map config) {
        if (dashboardString.contains(REPLACE_DASHBOARD_NAME)) {
            if (config.get("dashboardName") != null) {
                LOGGER.debug("replacing DashboardName: {}", config.get(DASHBOARD_NAME).toString());
                dashboardString = dashboardString.replace(REPLACE_DASHBOARD_NAME, config.get(DASHBOARD_NAME).toString());
            }
        }
        return dashboardString;
    }

    private static String replaceNodeName(String dashboardString, ControllerInfo controllerInfo) {
        if (dashboardString.contains(REPLACE_NODE_NAME)) {
            if (controllerInfo.getNodeName() != null) {
                dashboardString = dashboardString.replace(REPLACE_NODE_NAME, controllerInfo.getNodeName());
                LOGGER.debug("replacing NodeName: {}", controllerInfo.getNodeName());

            }
        }
        return dashboardString;
    }

    private static String replaceTierName(String dashboardString, ControllerInfo controllerInfo) {
        if (dashboardString.contains(REPLACE_TIER_NAME)) {
            if (controllerInfo.getTierName() != null) {
                dashboardString = dashboardString.replace(REPLACE_TIER_NAME, controllerInfo.getTierName());
                LOGGER.debug("replacing TierName: {}", controllerInfo.getTierName());

            }
        }
        return dashboardString;
    }

    private static String replaceApplicationName(String dashboardString, ControllerInfo controllerInfo) {
        if (dashboardString.contains(REPLACE_APPLICATION_NAME)) {
            if (controllerInfo.getApplicationName() != null) {

                dashboardString = dashboardString.replace(REPLACE_APPLICATION_NAME, controllerInfo.getApplicationName());
                LOGGER.debug("replacing ApplicationName : {}", controllerInfo.getApplicationName());

            }
        }
        return dashboardString;
    }

    private static String replaceMachinePath(String dashboardString, ControllerInfo controllerInfo) {
        if (dashboardString.contains(REPLACE_MACHINE_PATH)) {

            if (controllerInfo.getMachinePath() != null) {
                String machinePath = ROOT + METRICS_SEPARATOR + controllerInfo.getMachinePath();
                machinePath = machinePath.substring(0, machinePath.lastIndexOf(METRICS_SEPARATOR));

                dashboardString = dashboardString.replace(REPLACE_MACHINE_PATH, machinePath);
                LOGGER.debug("replacing MachinePath: {}", machinePath);

            } else {
                dashboardString = dashboardString.replace(REPLACE_MACHINE_PATH, ROOT);
                LOGGER.debug("replacing MachinePath: to default Root");

            }
        }
        return dashboardString;
    }
}
