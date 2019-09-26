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

import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.ControllerInfoFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomDashboardTemplateGeneratorTest {

    private File file = Mockito.mock(File.class);

    /*@Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = ControllerInfoFactory.class.getDeclaredField("controllerInfo");
        instance.setAccessible(true);
        instance.set(null, null);
    }*/

    @Test
    public void replaceDefaultValuesInTheNormalDashboard() throws Exception {
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(getConfigMap(), file);
        String metricPrefix = "Custom Metrics|Extension|";
        String dashboardName = "DashboardName";
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("dashboardName", "Dashboard Test");
        dashboardConfig.put("enabled", true);
        dashboardConfig.put("pathToNormalDashboard", "src/test/resources/dashboard/normalDashboard.json");
        String dashboardString = FileUtils.readFileToString(new File("src/test/resources/dashboard/normalDashboard.json"));
        CustomDashboardTemplateGenerator customDashboardGen = new CustomDashboardTemplateGenerator(dashboardConfig, controllerInfo, metricPrefix, dashboardName);
        String updatedDashboardString = customDashboardGen.getDashboardTemplate();
        Assert.assertFalse(dashboardString.equals(updatedDashboardString));
        if (dashboardString.contains(DashboardConstants.REPLACE_APPLICATION_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("applicationNameYML"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_SIM_APPLICATION_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains(DashboardConstants.SIM_APPLICATION_NAME));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_MACHINE_PATH)) {
            Assert.assertTrue(updatedDashboardString.contains("MachinePath"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_TIER_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("tierNameYML"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_NODE_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("Node"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)) {
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }
    }

    @Test
    public void replaceDefaultValuesInTheSIMDashboard() throws Exception {
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(getConfigMapForSim(), file);
        String metricPrefix = "Custom Metrics|Extension|";
        String dashboardName = "DashboardName";
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("dashboardName", "Dashboard Test");
        dashboardConfig.put("enabled", true);
        dashboardConfig.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");
        String dashboardString = FileUtils.readFileToString(new File("src/test/resources/dashboard/simDashboard.json"));
        CustomDashboardTemplateGenerator customDashboardGen = new CustomDashboardTemplateGenerator(dashboardConfig, controllerInfo, metricPrefix, dashboardName);
        String updatedDashboardString = customDashboardGen.getDashboardTemplate();
        Assert.assertFalse(dashboardString.equals(updatedDashboardString));
        if (dashboardString.contains(DashboardConstants.REPLACE_SIM_APPLICATION_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains(DashboardConstants.SIM_APPLICATION_NAME));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_MACHINE_PATH)) {
            Assert.assertTrue(updatedDashboardString.contains("Root"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_HOST_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("hostNameYML"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_UNIQUE_HOST_ID)) {
            Assert.assertTrue(updatedDashboardString.contains("uniqueHostIDYML"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)) {
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }
    }


    @Test
    public void verifyTheCorrectMachinePath() throws Exception {
        Map controllerInfoMapWithMachinePath = getConfigMapForSim();
        controllerInfoMapWithMachinePath.put("machinePath", "Test1|Test2|Test3|Test4");
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(controllerInfoMapWithMachinePath, file);
        String metricPrefix = "Custom Metrics|Extension|";
        String dashboardName = "DashboardName";
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("dashboardName", "Dashboard Test");
        dashboardConfig.put("enabled", true);
        dashboardConfig.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");
        String dashboardString = FileUtils.readFileToString(new File("src/test/resources/dashboard/simDashboard.json"));
        CustomDashboardTemplateGenerator customDashboardGen = new CustomDashboardTemplateGenerator(dashboardConfig, controllerInfo, metricPrefix, dashboardName);
        String updatedDashboardString = customDashboardGen.getDashboardTemplate();
        Assert.assertFalse(dashboardString.equals(updatedDashboardString));
        if (dashboardString.contains(DashboardConstants.REPLACE_SIM_APPLICATION_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains(DashboardConstants.SIM_APPLICATION_NAME));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_MACHINE_PATH)) {
            Assert.assertTrue(updatedDashboardString.contains("Root|Test1|Test2|Test3"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_UNIQUE_HOST_ID)) {
            Assert.assertTrue(updatedDashboardString.contains("uniqueHostIDYML"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_HOST_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("hostNameYML"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)) {
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }
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

    private Map getConfigMapForSim() {
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
        config.put("simEnabled", true);
        return config;
    }

}