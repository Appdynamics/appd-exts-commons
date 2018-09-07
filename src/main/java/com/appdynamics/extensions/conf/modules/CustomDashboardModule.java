package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);
    private ControllerInfo controllerInfo;

    public CustomDashboardModule() {
    }

    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, ControllerInfo controllerInfo) {
        logger.debug("Metric Prefix : {}", metricPrefix);
        this.controllerInfo = controllerInfo;
        logger.debug("Custom Dashboard Module controllerInfo : {}", controllerInfo);

        String dashboardMetricPrefix;
        Map customDashboardConfig = (Map) config.get("customDashboard");
        if (metricPrefix != null) {
            dashboardMetricPrefix = buildMetricPrefixForDashboard(metricPrefix);

            if ((customDashboardConfig != null && !customDashboardConfig.isEmpty())) {
                if ((Boolean) customDashboardConfig.get("enabled")) {

                    long startTime = System.currentTimeMillis();

                    // get data from generator
                    CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInfo, dashboardMetricPrefix);
                    dashboardGenerator.createDashboard();
                    String jsonExtension = "json";
                    String contentType = "application/json";
                    boolean overwrite;
                    if (customDashboardConfig.get("overwriteDashboard") != null) {
                        overwrite = (Boolean) customDashboardConfig.get("overwriteDashboard");
                    } else {
                        overwrite = false;
                    }

                    // Sending API service from module to maintain state
                    ControllerApiService apiService = new ControllerApiService(controllerInfo);

                    CustomDashboardUploader dashboardUploader = new CustomDashboardUploader(apiService);

                    CloseableHttpClient client = null;
                    try {

                        setProxyIfApplicable(dashboardGenerator.getHttpArgs());
                        // get client from httpclient
                        client = Http4ClientBuilder.getBuilder(dashboardGenerator.getHttpArgs()).build();

                        // send data and client to uploader
                        dashboardUploader.uploadDashboard(client, dashboardGenerator.getDashboardName(), jsonExtension, dashboardGenerator.getDashboardContent(), contentType, dashboardGenerator.getHttpArgs(), overwrite);
                    } catch (ApiException e) {
                        logger.error("Unable to establish connection, not uploading dashboard.");
                    } finally {
                        try {
                            client.close();
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    }

                    long endTime = System.currentTimeMillis();
                    logger.debug("Time to complete customDashboardModule in :" + (endTime - startTime) + " ms");
                } else {
                    logger.info("customDashboard is not enabled in config.yml, not uploading dashboard.");
                }
            } else {
                logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
            }
        } else {
            logger.info("No metricPrefix in config.yml, not uploading dashboard.");
        }
    }

    private void setProxyIfApplicable(Map<String, ? super Object> argsMap) {
        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");
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

    public String buildMetricPrefixForDashboard(String metricPrefix) {

        StringBuilder buildMetricPath = new StringBuilder();
        if (metricPrefix.contains("Server")) {
            String[] metricPath = metricPrefix.split("\\|");
            int count = 0;
            for (String str : metricPath) {
                count++;
                if (count > 2) {
                    buildMetricPath.append(str).append("|");
                }
            }
            buildMetricPath.deleteCharAt(buildMetricPath.length() - 1);
        } else {
            buildMetricPath.append(metricPrefix);
        }

        logger.debug("Dashboard Metric Prefix = " + buildMetricPath.toString());

        return buildMetricPath.toString();
    }

}
