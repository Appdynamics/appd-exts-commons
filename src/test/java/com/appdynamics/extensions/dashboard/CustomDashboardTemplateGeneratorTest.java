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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomDashboardTemplateGeneratorTest {

    /*@Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = ControllerInfoFactory.class.getDeclaredField("controllerInfo");
        instance.setAccessible(true);
        instance.set(null, null);
    }*/

    @Test
    public void replaceDefaultValuesInTheNormalDashboard() throws Exception {
        File installDir = new File("src/test/resources/dashboard");
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(getConfigMap(), installDir);
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
            Assert.assertTrue(updatedDashboardString.contains("xmlApplicationName"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_SIM_APPLICATION_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains(DashboardConstants.SIM_APPLICATION_NAME));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_MACHINE_PATH)) {
            Assert.assertTrue(updatedDashboardString.contains("xmlMachinePath"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_TIER_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("xmlTierName"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_NODE_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("xmlNodeName"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)) {
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }
    }

    @Test
    public void replaceDefaultValuesInTheSIMDashboard() throws Exception {
        File installDir = new File("src/test/resources/dashboard");
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(getConfigMap(), installDir);
        controllerInfo.setSimEnabled(true);
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
            Assert.assertTrue(updatedDashboardString.contains("xmlHost"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_UNIQUE_HOST_ID)) {
            Assert.assertTrue(updatedDashboardString.contains("xmlUniqueHostId"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)) {
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }
    }


    @Test
    public void verifyTheCorrectMachinePath() throws Exception {
        File installDir = new File("src/test/resources/dashboard");
        Map controllerInfoMapWithMachinePath = getConfigMap();
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(controllerInfoMapWithMachinePath, installDir);
        controllerInfo.setSimEnabled(true);
        controllerInfo.setMachinePath("Test1|Test2|Test3|Test4");
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
            Assert.assertTrue(updatedDashboardString.contains("xmlUniqueHostId"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_HOST_NAME)) {
            Assert.assertTrue(updatedDashboardString.contains("xmlHost"));
        }
        if (dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)) {
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }
    }

    private Map getConfigMap() {
        Map config = new HashMap<>();
        config.put("username", "usernameYML");
        config.put("password", "passwordYML");
        return config;
    }
}