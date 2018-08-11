/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.SendDashboard;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/9/18.
 */
public class DashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(DashboardModule.class);

    private ControllerInfo controllerInfo;

    public DashboardModule() {

        controllerInfo = new ControllerInfo().getControllerInfo();

    }

    public void initSendDashboard(Map<String, ?> config) {

        Map customDashboardConfig = (Map) config.get("customDashboard");
        if (customDashboardConfig != null) {
            long previousTimestamp = System.currentTimeMillis();

            SendDashboard dashboard = new SendDashboard(customDashboardConfig, new CustomDashboardUploader(), controllerInfo);
            dashboard.sendDashboard();

            long currentTimestamp = System.currentTimeMillis();
            logger.debug("Time to process Dashboard in milliseconds: " + (currentTimestamp - previousTimestamp));

            logger.info("Leaving Dashboard module");
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }

        ///////////// Dashboard Start Thread /////////////
//        LOGGER.debug("In Metric Processor going to upload dashboard");
//
//        Thread dashboardThread = new Thread(new Runnable() {
//            public void run() {
//                LOGGER.debug("Creating a new thread to send the dashboard");
//                dashboard.sendDashboard();
//            }
//        });
//        dashboardThread.start();
//
//        LOGGER.debug("Created Thread for Dashboard Upload");
        ///////////// Dashboard Stop  Thread /////////////

    }
}
