/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.conf.ControllerInfoFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.print.DocFlavor;
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
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(dashboardConfig, file);

        CustomDashboardModule customDashboardModule = new CustomDashboardModule(metric, controllerInfo);
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard();
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void getMetricPrefixFromNormalMetricPrefix() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB|");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(dashboardConfig, file);

        CustomDashboardModule customDashboardModule = new CustomDashboardModule( metric, controllerInfo);
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard();
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

    @Test
    public void verifyMetricPrefixWherePipeIsMissing() {
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("metricPrefix", "Custom Metrics|Amazon ELB");
        String metric = "Server|Component:21|Custom Metrics|Amazon ELB|";
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(dashboardConfig, file);

        CustomDashboardModule customDashboardModule = new CustomDashboardModule( metric, controllerInfo);
        String metricPrefix = customDashboardModule.buildMetricPrefixForDashboard();
        Assert.assertTrue(metricPrefix.equals("Custom Metrics|Amazon ELB"));
    }

}
