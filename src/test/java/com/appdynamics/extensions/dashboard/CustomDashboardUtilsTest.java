/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
