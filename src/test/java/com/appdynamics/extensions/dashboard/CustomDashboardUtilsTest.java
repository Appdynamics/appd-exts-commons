/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.ControllerInfoFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/28/18.
 */
public class CustomDashboardUtilsTest {

    @Test
    public void testBuildMetricPrefixForDashboardWithComponentID() {
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void whenMetricPrefixWithComponentAndNoTrailingPipeThenReturnWithNoTrailingPipe() {
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void whenMetricPrefixWithNoComponentAndNoTrailingPipeThenReturnWithNoTrailingPipe() {
        String metric = "Custom Metrics|Amazon ELB";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }


    @Test
    public void testBuildMetricPrefixForDashboardWithoutComponentID() {
        String metric = "Custom Metrics|Amazon ELB|";
        String metricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metric);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }


    @Test
    public void testGetOverwriteReturnsFalseWhenSentFalseFromConfig() {
        Map config = new HashMap();
        config.put("overwriteDashboard", false);
        Boolean check = CustomDashboardUtils.getOverwrite(config);
        Assert.assertFalse(check);
    }

    @Test
    public void testGetOverwriteReturnsFalseWhenNullInConfig() {
        Map config = new HashMap();
        Assert.assertFalse(CustomDashboardUtils.getOverwrite(config));
    }

    @Test
    public void testGetDashboardNameWithValuePresentInConfig() {
        Map config = new HashMap();
        config.put("dashboardName", "Name");
        String monitor = "Monitor";
        String name = CustomDashboardUtils.getDashboardName(config, monitor);
        Assert.assertTrue(name.equals("Name"));
    }

    @Test
    public void testGetDashboardNameWithValueNotPresentInConfig() {
        Map config = new HashMap();
        String monitor = "MonitorName";
        String name = CustomDashboardUtils.getDashboardName(config, monitor);
        Assert.assertTrue(name.equals("MonitorName Dashboard"));
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
