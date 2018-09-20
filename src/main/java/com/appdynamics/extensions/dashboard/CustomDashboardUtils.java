/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;

/**
 * Created by bhuvnesh.kumar on 9/18/18.
 */
public class CustomDashboardUtils {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardUtils.class);

    public static String getDashboardName(Map customDashboardConfig, String monitorName) {
        String dashboardName;
        if (!Strings.isNullOrEmpty((String) customDashboardConfig.get("dashboardName"))) {
            dashboardName = customDashboardConfig.get("dashboardName").toString();
        } else {
            dashboardName = monitorName + " Dashboard";
        }
        return dashboardName;
    }

    public static boolean getOverwrite(Map customDashboardConfig) {
        if (customDashboardConfig.get("overwriteDashboard") != null) {
            return (Boolean) customDashboardConfig.get("overwriteDashboard");
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
            dashboardMetricPrefix.append(metricPrefix);
        }
        logger.debug("Dashboard Metric Prefix = " + dashboardMetricPrefix.toString());
        return dashboardMetricPrefix.toString();
    }

    public static Map<String, ? super Object> getHttpProperties(ControllerInfo controllerInfo, Map config) {
        Map customDashboardConfig = (Map) config.get("customDashboard");
        Map<String, ? super Object> httpProperties = new HashMap<>();
        List<Map<String, ? super Object>> controllerServersList = Lists.newArrayList();
        Map<String, ? super Object> controllerServerInfo = getControllerServerDetailsFromControllerInfo(controllerInfo);
        controllerServersList.add(controllerServerInfo);
        httpProperties.put("servers", controllerServersList);
        Map<String, ? super Object> connectionMap = getConnectionMap(customDashboardConfig);
        httpProperties.put("connection", connectionMap);
        setProxyIfApplicable(httpProperties, config);
        return httpProperties;
    }

    private static Map<String, ? super Object> getControllerServerDetailsFromControllerInfo(ControllerInfo controllerInfo) {
        Map<String, ? super Object> controllerServer = new HashMap<>();
        controllerServer.put(TaskInputArgs.HOST, controllerInfo.getControllerHost());
        controllerServer.put(TaskInputArgs.PORT, String.valueOf(controllerInfo.getControllerPort()));
        controllerServer.put(TaskInputArgs.USE_SSL, String.valueOf(controllerInfo.getControllerSslEnabled()));
        controllerServer.put(TaskInputArgs.USER, getUserName(controllerInfo));
        controllerServer.put(TaskInputArgs.PASSWORD, controllerInfo.getPassword());
        return controllerServer;
    }

    private static Map<String, ? super Object> getConnectionMap(Map customDashboardConfig) {
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

    private static String getUserName(ControllerInfo controllerInfo) {
        String accountName = controllerInfo.getAccount();
        String username = controllerInfo.getUsername();
        if (accountName != null && username != null) {
            return username + AT + accountName;
        }
        return "";
    }

    private static void setProxyIfApplicable(Map<String, ? super Object> argsMap, Map config) {
        Map proxyConfig = (Map) config.get("proxy");
        String proxyHost = "";
        String proxyPort = "";
        String proxyUsername = "";
        String proxyPassword = "";
        // getting from system properties
        if (Strings.isNullOrEmpty(System.getProperty("appdynamics.http.proxyHost"))) {
            proxyHost = System.getProperty("appdynamics.http.proxyHost");
        }
        if (Strings.isNullOrEmpty(System.getProperty("appdynamics.http.proxyPort"))) {
            proxyPort = System.getProperty("appdynamics.http.proxyPort");
        }
        if (Strings.isNullOrEmpty(System.getProperty("appdynamics.http.proxyUsername"))) {
            proxyUsername = System.getProperty("appdynamics.http.proxyUsername");
        }
        if (Strings.isNullOrEmpty(System.getProperty("appdynamics.http.proxyPassword"))) {
            proxyPassword = System.getProperty("appdynamics.http.proxyPassword");
        }
        // overwriting if config has values
        if (proxyConfig != null) {
            if (Strings.isNullOrEmpty((String) proxyConfig.get(TaskInputArgs.HOST))) {
                proxyHost = proxyConfig.get(TaskInputArgs.HOST).toString();
            }
            if (Strings.isNullOrEmpty((String) proxyConfig.get(TaskInputArgs.PORT))) {
                proxyPort = proxyConfig.get(TaskInputArgs.PORT).toString();
            }
            if (Strings.isNullOrEmpty((String) proxyConfig.get(TaskInputArgs.USER))) {
                proxyUsername = proxyConfig.get(TaskInputArgs.USER).toString();
            }
            if (Strings.isNullOrEmpty((String) proxyConfig.get(TaskInputArgs.PASSWORD))) {
                proxyPassword = proxyConfig.get(TaskInputArgs.PASSWORD).toString();
            }
        }
        // adding proxy information to HTTP Properties
        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {
            Map<String, ? super Object> proxyMap = new HashMap<>();
            proxyMap.put(TaskInputArgs.HOST, proxyHost);
            proxyMap.put(TaskInputArgs.PORT, proxyPort);
            logger.debug("Using the proxy {}:{} to upload the dashboard", proxyHost, proxyPort);
            if (StringUtils.hasText(proxyUsername) && StringUtils.hasText(proxyPassword)) {
                proxyMap.put(TaskInputArgs.USER, proxyUsername);
                proxyMap.put(TaskInputArgs.PASSWORD, proxyPassword);
            }
            argsMap.put("proxy", proxyMap);
            logger.debug("Using the proxy {}:{} to upload the dashboard", proxyHost, proxyPort);
        } else {
            logger.debug("Not using proxy for dashboard upload appdynamics.http.proxyHost={} and appdynamics.http.proxyPort={}"
                    , proxyHost, proxyPort);
        }
    }
}
