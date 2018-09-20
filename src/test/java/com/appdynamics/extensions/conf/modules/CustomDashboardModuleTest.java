/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 9/19/18.
 */
public class CustomDashboardModuleTest {

    @Test
    public  void testSendDashboard(){
        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        Map config = new HashMap<>();
        config.put("customDashboard", getCustomDashboardMap());
        config.put("controllerInfo", getControllerInfoMap());
        String metric = "Custom Metrics|MonitorName";
        String monitorName = "MonitorName";
        File file = Mockito.mock(File.class);

        ControllerInfo controllerInfo;
        ControllerInfoFactory.initialize(getControllerInfoMap(), file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();
        customDashboardModule.initCustomDashboard(config, metric, monitorName, controllerInfo);

    }

    private Map getCustomDashboardMap() {
        Map config = new HashMap<>();
        config.put("enabled", true);
        config.put("dashboardName", "MonitorName");
        config.put("pathToSIMDashboard", "monitors/AWSELBMonitor_dash/simDashboard.json");
        config.put("pathToNormalDashboard", "monitors/AWSELBMonitor_dash/normalDashboard.json");
        return config;
    }


    private Map getControllerInfoMap() {
        Map config = new HashMap<>();
        config.put("controllerHost", "hostNameYML");
        config.put("controllerPort", 9999);
        config.put("controllerSslEnabled", false);
        config.put("uniqueHostId", "uniqueHostIDYML");
        config.put("account", "accountNameYML");
        config.put("username", "usernameYML");
        config.put("password", "passwordYML");
        config.put("accountAccessKey", "accessKeyYML");
        config.put("applicationName", "applicationNameYML");
        config.put("tierName", "tierNameYML");
        config.put("nodeName", "nodeNameYML");
        config.put("simEnabled", false);
        return config;
    }
}