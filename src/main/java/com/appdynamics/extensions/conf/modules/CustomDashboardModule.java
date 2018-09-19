package com.appdynamics.extensions.conf.modules;

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
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.util.Map;

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
            sendDashboardDataToUploader(controllerInfo, dashboardName, overwrite, dashboardTemplate, config);
        } else {
            logger.error("All required fields not resolved to upload dashboard.");
        }
    }

    private void sendDashboardDataToUploader(ControllerInfo controllerInfo, String dashboardName, boolean overwrite, String dashboardTemplate, Map config) {
        ControllerApiService apiService = new ControllerApiService(controllerInfo);
        Map httpProperties = CustomDashboardUtils.getHttpProperties(controllerInfo, config);
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
}
