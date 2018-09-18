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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/28/18.
 */
public class CustomDashboardModuleTest {

    private File file = Mockito.mock(File.class);

//    static Logger logger;
//
//    @BeforeClass
//    public static void init() {
//        PowerMockito.mockStatic(LoggerFactory.class);
//        logger = PowerMockito.mock(Logger.class);
//        PowerMockito.when(ExtensionsLoggerFactory.getLogger(MonitorExecutorServiceModule.class)).thenReturn(logger);
//    }

    @Test
    public void getMetricPrefixFromTierInMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Server|Component:21|Custom Metrics|Amazon ELB|");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";

        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void getMetricPrefixFromNormalMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB|");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";

        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void verifyMetricPrefixWherePipeIsMissing() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";

        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void testInitCustomDashboard() {
        String metricPrefix = "Server|Component:21|Custom Metrics|Amazon ELB|";
        setupSystemProps();
        Map config = getConfigMap();
        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(getConfigMap(), file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        customDashboardModule.initCustomDashboard(config, metricPrefix, controllerInfo);

//        verify(logger, atLeastOnce()).debug("Queue Capacity reached!! Rejecting runnable tasks..");


    }

    private Map getConfigMap() {
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

    public void setupSystemProps() {

        System.setProperty("appdynamics.agent.accountAccessKey", "accessKey");
        System.setProperty("appdynamics.agent.accountName", "accountName");
        System.setProperty("appdynamics.agent.applicationName", "applicationName");
        System.setProperty("appdynamics.agent.tierName", "tierName");
        System.setProperty("appdynamics.agent.nodeName", "nodeName");
        System.setProperty("appdynamics.controller.hostName", "hostName");
        System.setProperty("appdynamics.agent.monitors.controller.username", "username");
        System.setProperty("appdynamics.agent.monitors.controller.password", "password");
        System.setProperty("appdynamics.agent.monitors.controller.encryptionKey", "encryptionKey");
        System.setProperty("appdynamics.agent.monitors.controller.encryptedPassword", "encryptedPassword");
        System.setProperty("appdynamics.controller.port", "9090");
        System.setProperty("appdynamics.controller.ssl.enabled", "false");
        System.setProperty("appdynamics.agent.uniqueHostId", "uniqueHostID");
        System.setProperty("appdynamics.sim.enabled", "false");

    }

}
