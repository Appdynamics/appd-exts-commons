package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);
    private String metricPrefix;
    private ControllerInfo controllerInfo;

    public CustomDashboardModule( String metricPrefix, ControllerInfo controllerInfo) {
        this.metricPrefix = metricPrefix;
        this.controllerInfo = controllerInfo;
    }

    // todo generator should generate, uploader should upload
    // todo create a client here and send that client to that uploader instead of doing it in the uploader
    // todo send a map of string string to the uploader and also the client from here

    // get data from generator
    // get client from httpclient
    // send both to uploader
    public void initCustomDashboard(Map<String, ?> config) {
        String dashboardMetricPrefix;
        Map customDashboardConfig = (Map) config.get("customDashboard");
        if(metricPrefix != null){
            dashboardMetricPrefix = buildMetricPrefixForDashboard();

            if ((customDashboardConfig != null && !customDashboardConfig.isEmpty())) {
                if ((Boolean)customDashboardConfig.get("enabled")  ) {
                    long startTime = System.currentTimeMillis();
                    CustomDashboardUploader uploader = new CustomDashboardUploader();
                    CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInfo, dashboardMetricPrefix, uploader);
                    dashboardGenerator.createDashboard();
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

    public String buildMetricPrefixForDashboard() {

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
            buildMetricPath.deleteCharAt(buildMetricPath.length() -1);
        } else {
            buildMetricPath.append(metricPrefix);
        }

        logger.debug("Dashboard Metric Prefix = " + buildMetricPath.toString());

        return buildMetricPath.toString();
    }

}
