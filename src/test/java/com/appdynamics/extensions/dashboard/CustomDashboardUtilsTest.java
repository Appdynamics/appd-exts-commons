/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        String name = CustomDashboardUtils.getDashboardName(config);
        Assert.assertTrue(name.equals("Name"));
    }

    @Test
    public void verifyGetDashboardNameWithoutValue() {
        Map config = new HashMap();
        String name = CustomDashboardUtils.getDashboardName(config);
        Assert.assertTrue(name.equals("Custom Dashboard"));
    }

}
