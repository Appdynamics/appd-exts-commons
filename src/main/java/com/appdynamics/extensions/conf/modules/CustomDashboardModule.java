package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoValidator;
import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.DashboardConstants;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);

    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, ControllerInfo controllerInfo) {
        logger.debug("Custom Dashboard Module controllerInfo : {}", controllerInfo);
        Map customDashboardConfig = (Map) config.get("customDashboard");
        Map proxyMap = (Map) config.get("proxy");

        if ((customDashboardConfig != null && !customDashboardConfig.isEmpty())) {
            if ((Boolean) customDashboardConfig.get(DashboardConstants.ENABLED)) {
                long startTime = System.currentTimeMillis();
                gatherDashboardData(metricPrefix, controllerInfo, customDashboardConfig, proxyMap);
                long endTime = System.currentTimeMillis();
                logger.debug("Time to complete customDashboardModule in :" + (endTime - startTime) + " ms");
            } else {
                logger.info("customDashboard is not enabled in config.yml, not uploading dashboard.");
            }
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }
    }

    private void gatherDashboardData(String metricPrefix, ControllerInfo controllerInfo, Map customDashboardConfig, Map proxyMap) {
        ControllerInfoValidator validator = new ControllerInfoValidator();
        if (validator.isValidatedAndResolved(controllerInfo)) {
            String dashboardName = getDashboardName(customDashboardConfig);
            boolean overwrite = getOverwrite(customDashboardConfig);
            String dashboardMetricPrefix = buildMetricPrefixForDashboard(metricPrefix);
            // get data from generator
            CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInfo, dashboardMetricPrefix, dashboardName);
            String dashboardTemplate = dashboardGenerator.getDashboardTemplate();
            sendDashboardDataToUploader(controllerInfo, customDashboardConfig, dashboardName, overwrite, dashboardTemplate, proxyMap);

        } else {
            logger.error("All required fields not resolved to upload dashboard. ");
        }
    }

    private void sendDashboardDataToUploader(ControllerInfo controllerInfo, Map customDashboardConfig, String dashboardName, boolean overwrite, String dashboardTemplate, Map proxyMap) {
        // Sending API service from module to maintain state
        ControllerApiService apiService = new ControllerApiService(controllerInfo);
 // todo pass the complete config here, then get proxy from that
        Map httpProperties = getHttpProperties(customDashboardConfig, controllerInfo);
        CloseableHttpClient client = null;
        setProxyIfApplicable(httpProperties, proxyMap);
        try {
            client = Http4ClientBuilder.getBuilder(httpProperties).build();
            // send data and client to uploader
            CustomDashboardUploader dashboardUploader = new CustomDashboardUploader();
            dashboardUploader.gatherDashboardDataToUpload(apiService, client, dashboardName, dashboardTemplate, httpProperties, overwrite);
        } catch (ApiException e) {
            logger.error("Unable to establish connection, not uploading dashboard.");
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("Error encountered while closing the HTTP Client for dashboard.", e);
            }
        }
    }

    private String getDashboardName(Map dashboardConfig) {
        String dashboardName;
        if (!Strings.isNullOrEmpty((String) dashboardConfig.get("dashboardName"))) {
            dashboardName = dashboardConfig.get("dashboardName").toString();
        } else {
            dashboardName = "Custom Dashboard";
        }
        dashboardConfig.put("dashboardName", dashboardName);
        return dashboardName;
    }

    private boolean getOverwrite(Map customDashboardConfig) {
        boolean overwrite;
        if (customDashboardConfig.get("overwriteDashboard") != null) {
            overwrite = (Boolean) customDashboardConfig.get("overwriteDashboard");
        } else {
            overwrite = false;
        }
        return overwrite;
    }

// todo make private
    public String buildMetricPrefixForDashboard(String metricPrefix) {
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

    private Map<String, ? super Object> getHttpProperties(Map customDashboardConfig, ControllerInfo controllerInfo) {
        Map<String, ? super Object> httpProperties = new HashMap<>();
        List<Map<String, ? super Object>> serverList = Lists.newArrayList();
        Map<String, ? super Object> serverMap = getControllerServerDetailsFromControllerInfo(controllerInfo);
        serverList.add(serverMap);
        httpProperties.put("servers", serverList);
        Map<String, ? super Object> connectionMap = getConnectionMap(customDashboardConfig);
        httpProperties.put("connection", connectionMap);
        // todo call for proxy here
        return httpProperties;
    }

    private Map<String, ? super Object> getControllerServerDetailsFromControllerInfo(ControllerInfo controllerInfo) {
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, controllerInfo.getControllerHost());
        serverMap.put(TaskInputArgs.PORT, String.valueOf(controllerInfo.getControllerPort()));
        serverMap.put(TaskInputArgs.USE_SSL, String.valueOf(controllerInfo.getControllerSslEnabled()));
        serverMap.put(TaskInputArgs.USER, getUserName(controllerInfo));
        serverMap.put(TaskInputArgs.PASSWORD, controllerInfo.getPassword());
        logger.debug("Controller Info: ");
        logger.debug(TaskInputArgs.HOST + ": {}", controllerInfo.getControllerHost());
        logger.debug(TaskInputArgs.PORT + ": {}", String.valueOf(controllerInfo.getControllerPort()));
        logger.debug(TaskInputArgs.USE_SSL + ": {}", controllerInfo.getControllerSslEnabled());
        logger.debug(TaskInputArgs.USER + ": {}", getUserName(controllerInfo));
        return serverMap;
    }

    private Map<String, ? super Object> getConnectionMap(Map customDashboardConfig) {
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

    private String getUserName(ControllerInfo controllerInfo) {
        String accountName = controllerInfo.getAccount();
        String username = controllerInfo.getUsername();
        if (accountName != null && username != null) {
            return username + AT + accountName;
        }
        return "";
    }

    private void setProxyIfApplicable(Map<String, ? super Object> argsMap, Map proxyConfig) {
        // getting from system properties
        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");
// todo check if u need username and password
        // getting from config.yml
        String proxyHostConfig = proxyConfig.get(TaskInputArgs.HOST).toString();
        String proxyPortConfig = proxyConfig.get(TaskInputArgs.PORT).toString();

        // overwriting if config has values
        if (Strings.isNullOrEmpty(proxyHostConfig) && Strings.isNullOrEmpty(proxyPortConfig)) {
            proxyHost = proxyHostConfig;
            proxyPort = proxyPortConfig;
        }

        // adding proxy information to HTTP Properties
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
