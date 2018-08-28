package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
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


        if (customDashboardConfig != null) {
            long timestamp1 = System.currentTimeMillis();
            CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInformation, metricPrefix);
            dashboardGenerator.createDashboard();
            long timestamp2 = System.currentTimeMillis();
            logger.debug("Time to complete customDashboardModule in :" + (timestamp2 - timestamp1) + " ms");
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }
    }

    private String buildMetricPrefixForDashboard(Map<String, ?> config) {
        String metricPrefix = "";
        if(config.get("metricPrefix") != null){
            metricPrefix = config.get("metricPrefix").toString();
        }
        String text = "";
        if(metricPrefix.contains("Server")){
            String[] metricPath = metricPrefix.split("\\|");
            int count = 0;
            for(String str: metricPath){
                count++;
                if (count > 2){
                    text += str+ "|";
                }
            }
        }
        logger.debug("Dashboard Metric Prefix = " + metricPrefix);
        return text;
    }


}
