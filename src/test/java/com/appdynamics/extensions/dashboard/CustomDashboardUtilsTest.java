/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.SSL_CERT_CHECK_ENABLED;

/**
 * Created by bhuvnesh.kumar on 8/28/18.
 */
public class CustomDashboardUtilsTest {

    @Test
    public void getMetricPrefixFromTierInMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Server|Component:21|Custom Metrics|Amazon ELB|");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void getMetricPrefixFromNormalMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB|");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void verifyMetricPrefixWherePipeIsMissing() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void verifyGetOverwriteFalse() {
        Map config = new HashMap();
        config.put("overwriteDashboard", false);
        Boolean check = CustomDashboardUtils.getOverwrite(config);
        Assert.assertFalse(check);
    }

    @Test
    public void verifyGetOverwriteEmptyReturnsFalse() {
        Map config = new HashMap();
        Boolean check = CustomDashboardUtils.getOverwrite(config);
        Assert.assertFalse(check);
    }

    @Test
    public void verifyGetDashboardNameWithValue() {
        Map config = new HashMap();
        config.put("dashboardName", "Name");
        String monitor = "Monitor";
        String name = CustomDashboardUtils.getDashboardName(config, monitor);
        Assert.assertTrue(name.equals("Name"));
    }

    @Test
    public void verifyGetDashboardNameWithoutValue() {
        Map config = new HashMap();
        String monitor = "MonitorName";
        String name = CustomDashboardUtils.getDashboardName(config, monitor);
        Assert.assertTrue(name.equals("MonitorName Dashboard"));
    }

    @Test
    public void testGetHttpProperties() {
        Map config = new HashMap();
        Map customDashboardConfig = new HashMap();
        customDashboardConfig.put(SSL_CERT_CHECK_ENABLED, false);
        config.put("customDashboard", customDashboardConfig);
        File file = Mockito.mock(File.class);
        ControllerInfo controllerInfo;
        ControllerInfoFactory.initialize(getConfigMap(), file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();
        Map httpProperties = CustomDashboardUtils.getHttpProperties(controllerInfo, config);
        ArrayList servers = (ArrayList) httpProperties.get("servers");
        Map controllerServer = (Map) servers.get(0);
        Map connection = (Map) httpProperties.get("connection");
        Assert.assertTrue(controllerServer.get("password").toString().equals("passwordYML"));
        Assert.assertTrue(controllerServer.get("username").toString().equals("usernameYML@accountNameYML"));
        Assert.assertTrue(controllerServer.get("port").toString().equals("9999"));
        Assert.assertTrue(controllerServer.get("host").toString().equals("hostNameYML"));
        Assert.assertTrue(connection.get("sslCertCheckEnabled").toString().equals("false"));
        Assert.assertTrue(connection.get("connectTimeout").toString().equals("10000"));
        Assert.assertTrue(connection.get("socketTimeout").toString().equals("15000"));
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

}
