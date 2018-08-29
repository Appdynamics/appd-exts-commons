package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);

    public void initCustomDashboard(Map<String, ?> config) {
        Map customDashboardConfig = new HashMap();
        Map controllerInformation = new HashMap();
        if(config.get("customDashboard") != null){
            customDashboardConfig = (Map) config.get("customDashboard");
        }
        if (config.get("controllerInfo") != null){
            controllerInformation = (Map) config.get("controllerInfo");
        }

        String metricPrefix = buildMetricPrefixForDashboard(config);

        if(!metricPrefix.equals("") ){
            if (customDashboardConfig != null) {
                long timestamp1 = System.currentTimeMillis();
                CustomDashboardUploader uploader = new CustomDashboardUploader();
                CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInformation, metricPrefix,uploader);
                dashboardGenerator.createDashboard();
                long timestamp2 = System.currentTimeMillis();
                logger.debug("Time to complete customDashboardModule in :" + (timestamp2 - timestamp1) + " ms");
            } else {
                logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
            }
        } else {
            logger.info("No metricPrefix Info in config.yml, not uploading dashboard.");
        }
    }

    public String buildMetricPrefixForDashboard(Map<String, ?> config) {
        String metricPrefix = "";
        if(config.get("metricPrefix") != null){
            metricPrefix = config.get("metricPrefix").toString();
        }
        String dashboardMetricPath = "";
        if(metricPrefix.contains("Server")){
            String[] metricPath = metricPrefix.split("\\|");
            int count = 0;
            for(String str: metricPath){
                count++;
                if (count > 2){
                    dashboardMetricPath += str+ "|";
                }
            }
        } else {
            dashboardMetricPath = metricPrefix;
        }

        if(!dashboardMetricPath.endsWith("|")){
            dashboardMetricPath += "|";
        }

        logger.debug("Dashboard Metric Prefix = " + dashboardMetricPath);

        return dashboardMetricPath;
    }


}
