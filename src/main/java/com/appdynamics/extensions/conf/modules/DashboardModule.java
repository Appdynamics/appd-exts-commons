/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.dashboard.CustomDashboardJsonUploader;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import com.appdynamics.extensions.dashboard.SendDashboard;
/**
 * Created by bhuvnesh.kumar on 8/9/18.
 */
public class DashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(DashboardModule.class);

    private ControllerInfo controllerInfo;

    public DashboardModule(){

        controllerInfo = new ControllerInfo().getControllerInfo();

    }

    public void initSendDashboard(Map<String, ?> config){

        Map customDashboardConfig = (Map) config.get("customDashboard");
        if(customDashboardConfig != null){
            SendDashboard dashboard = new SendDashboard(customDashboardConfig, new CustomDashboardJsonUploader(), controllerInfo);
            dashboard.sendDashboard();

            logger.info("Dashboard sent");
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }


    }
}
