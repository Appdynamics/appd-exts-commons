/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

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

    @Test
    public void getMetricPrefixFromTierInMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Server|Component:21|Custom Metrics|Amazon ELB|");

        CustomDashboardModule customDashboardModule = new CustomDashboardModule(file);
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard(dashboardConfig);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB|"));
    }

    @Test
    public void getMetricPrefixFromNormalMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB|");
        CustomDashboardModule customDashboardModule = new CustomDashboardModule(file);
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard(dashboardConfig);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB|"));
    }

    @Test
    public void addPipeToMetricPrefixWhereMissing() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB");
        CustomDashboardModule customDashboardModule = new CustomDashboardModule(file);
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard(dashboardConfig);
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB|"));
    }

}
