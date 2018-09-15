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

    public CustomDashboardModule() {
    }

    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, ControllerInfo controllerInfo) {
        logger.debug("Custom Dashboard Module controllerInfo : {}", controllerInfo);
        String dashboardMetricPrefix;
        Map customDashboardConfig = (Map) config.get("customDashboard");
        dashboardMetricPrefix = buildMetricPrefixForDashboard(metricPrefix);

        if ((customDashboardConfig != null && !customDashboardConfig.isEmpty())) {
            if ((Boolean) customDashboardConfig.get(DashboardConstants.ENABLED)) {
                long startTime = System.currentTimeMillis();

                if (isResolved(controllerInfo)) {

                // get data from generator
                CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInfo, dashboardMetricPrefix);
                dashboardGenerator.createDashboard();
                boolean overwrite = getOverwrite(customDashboardConfig);

                // Sending API service from module to maintain state
                ControllerApiService apiService = new ControllerApiService(controllerInfo);

                CustomDashboardUploader dashboardUploader = new CustomDashboardUploader(apiService, controllerInfo);

                try {
                    // send data and client to uploader
                    dashboardUploader.uploadDashboard( getDashboardName(customDashboardConfig), dashboardGenerator.getDashboardContent(), customDashboardConfig,  overwrite);
                } catch (ApiException e) {
                    logger.error("Unable to establish connection, not uploading dashboard.");
                }

            } else {
                logger.error("Unable to establish connection, please make sure you have provided all necessary values.");
            }
                long endTime = System.currentTimeMillis();
                logger.debug("Time to complete customDashboardModule in :" + (endTime - startTime) + " ms");
            } else {
                logger.info("customDashboard is not enabled in config.yml, not uploading dashboard.");
            }
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }

    }

    protected boolean isResolved(ControllerInfo controllerInfo) {
        ControllerInfoValidator validator = new ControllerInfoValidator();
        return validator.validateAndCheckIfResolved(controllerInfo);
    }


    private String getDashboardName(Map dashboardConfig) {
        String dashboardName  ;
        if(!Strings.isNullOrEmpty((String)dashboardConfig.get("dashboardName"))){
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
        }
        else {
            buildMetricPath.append(metricPrefix);
        }

        logger.debug("Dashboard Metric Prefix = " + buildMetricPath.toString());

        return buildMetricPath.toString();
    }


}
