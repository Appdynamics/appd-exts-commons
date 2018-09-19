package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoValidator;
import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.CustomDashboardUtils;
import com.appdynamics.extensions.dashboard.DashboardConstants;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
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
        if ((customDashboardConfig != null && !customDashboardConfig.isEmpty())) {
            if ((Boolean) customDashboardConfig.get(DashboardConstants.ENABLED)) {
                long startTime = System.currentTimeMillis();
                gatherDashboardData(metricPrefix, controllerInfo, customDashboardConfig, config);
                long endTime = System.currentTimeMillis();
                logger.debug("Time to complete customDashboardModule in :" + (endTime - startTime) + " ms");
            } else {
                logger.info("customDashboard is not enabled in config.yml, not uploading dashboard.");
            }
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }
    }

    private void gatherDashboardData(String metricPrefix, ControllerInfo controllerInfo, Map customDashboardConfig, Map config) {
        ControllerInfoValidator validator = new ControllerInfoValidator();
        if (validator.isValidatedAndResolved(controllerInfo)) {
            String dashboardName = CustomDashboardUtils.getDashboardName(customDashboardConfig);
            boolean overwrite = CustomDashboardUtils.getOverwrite(customDashboardConfig);
            String dashboardMetricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metricPrefix);
            CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInfo, dashboardMetricPrefix, dashboardName);
            String dashboardTemplate = dashboardGenerator.getDashboardTemplate();
            sendDashboardDataToUploader(controllerInfo, customDashboardConfig, dashboardName, overwrite, dashboardTemplate, config);
        } else {
            logger.error("All required fields not resolved to upload dashboard.");
        }
    }

    private void sendDashboardDataToUploader(ControllerInfo controllerInfo, Map customDashboardConfig, String dashboardName, boolean overwrite, String dashboardTemplate, Map config) {
        ControllerApiService apiService = new ControllerApiService(controllerInfo);
        Map httpProperties = getHttpProperties(customDashboardConfig, controllerInfo, config);
        CloseableHttpClient client = null;
        try {
            client = Http4ClientBuilder.getBuilder(httpProperties).build();
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

    private Map<String, ? super Object> getHttpProperties(Map customDashboardConfig, ControllerInfo controllerInfo, Map config) {
        Map<String, ? super Object> httpProperties = new HashMap<>();
        List<Map<String, ? super Object>> serverList = Lists.newArrayList();
        Map<String, ? super Object> serverMap = getControllerServerDetailsFromControllerInfo(controllerInfo);
        serverList.add(serverMap);
        httpProperties.put("servers", serverList);
        Map<String, ? super Object> connectionMap = getConnectionMap(customDashboardConfig);
        httpProperties.put("connection", connectionMap);
        setProxyIfApplicable(httpProperties, config);
        return httpProperties;
    }

    private Map<String, ? super Object> getControllerServerDetailsFromControllerInfo(ControllerInfo controllerInfo) {
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, controllerInfo.getControllerHost());
        serverMap.put(TaskInputArgs.PORT, String.valueOf(controllerInfo.getControllerPort()));
        serverMap.put(TaskInputArgs.USE_SSL, String.valueOf(controllerInfo.getControllerSslEnabled()));
        serverMap.put(TaskInputArgs.USER, getUserName(controllerInfo));
        serverMap.put(TaskInputArgs.PASSWORD, controllerInfo.getPassword());
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
        AssertUtils.assertNotNull(controllerInfo.getUsername(), "Unable to get a valid Username for uploading Dashboard");
        AssertUtils.assertNotNull(controllerInfo.getAccount(), "Unable to get a valid Account for uploading Dashboard");
        return controllerInfo.getUsername() + AT + controllerInfo.getAccount();
    }

    private void setProxyIfApplicable(Map<String, ? super Object> argsMap, Map config) {
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
        // getting from config.yml
        String proxyHostConfig = proxyConfig.get(TaskInputArgs.HOST).toString();
        String proxyPortConfig = proxyConfig.get(TaskInputArgs.PORT).toString();
        String proxyUsernameConfig = proxyConfig.get(TaskInputArgs.USER).toString();
        String proxyPasswordConfig = proxyConfig.get(TaskInputArgs.PASSWORD).toString();
        // overwriting if config has values
        if (Strings.isNullOrEmpty(proxyHostConfig)) {
            proxyHost = proxyHostConfig;
        }
        if (Strings.isNullOrEmpty(proxyPortConfig)) {
            proxyPort = proxyPortConfig;
        }
        if (Strings.isNullOrEmpty(proxyUsernameConfig)) {
            proxyUsername = proxyUsernameConfig;
        }
        if (Strings.isNullOrEmpty(proxyPasswordConfig)) {
            proxyPassword = proxyPasswordConfig;
        }
        // adding proxy information to HTTP Properties
        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort) &&
                StringUtils.hasText(proxyUsername) && StringUtils.hasText(proxyPassword)) {
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
