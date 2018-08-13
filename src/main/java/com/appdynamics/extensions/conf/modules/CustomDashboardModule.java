package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.SendDashboard;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);

    public void initCustomDashboard(ControllerInfo controllerInfo,Map<String, ?> config){

        Map customDashboardConfig = (Map) config.get("customDashboard");
        if(customDashboardConfig != null){
            //TODO add timing in debug mode
            CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator();
            dashboardGenerator.createDashboard();
            logger.info("Dashboard sent");
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }


    }

}
