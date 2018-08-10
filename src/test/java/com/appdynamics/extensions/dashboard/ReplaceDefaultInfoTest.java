/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.conf.ControllerInfo;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;
/**
 * Created by bhuvnesh.kumar on 8/8/18.
 */
public class ReplaceDefaultInfoTest {

    private String getSIMJsonAsString() {
        String dashboardJson = "";
        try {
             dashboardJson = FileUtils.readFileToString(new File("src/test/resources/dashboard/simDashboard.json"));

        } catch (Exception e) {
            Assert.fail();
        }
        return dashboardJson;
    }


    private Map valueMapWithSim(Map config) {
        config.put("enabled", "true");
        config.put("dashboardName", "testDashboardName");
        config.put("uploadDashboard", "true");
        config.put("executionFrequencyMinutes", "5");
        config.put("password", "root");
        config.put("username", "user");
        config.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");

        return config;
    }

    private ControllerInfo setUpControllerInfoWithSim(ControllerInfo controllerInfo) {
        controllerInfo.setUsername("CTRLadmin");
        controllerInfo.setPassword("CTRLroot");
        controllerInfo.setAccountAccessKey("CTRLAccessKey");
        controllerInfo.setAccount("CTRLcustomer1");
        controllerInfo.setControllerHost("CTRLHost");
        controllerInfo.setControllerPort(9999);
        controllerInfo.setControllerSslEnabled(false);
        controllerInfo.setMachinePath("CTRLMachinePath|Something");
        controllerInfo.setSimEnabled(true);
        return controllerInfo;
    }

    private String getAppTierNodeJsonAsString() {
        String dashboardJson = "";
        try {
            dashboardJson = FileUtils.readFileToString(new File("src/test/resources/dashboard/normalDashboard.json"));

        } catch (Exception e) {
            Assert.fail();
        }
        return dashboardJson;
    }

    private  ControllerInfo setUpControllerInfoWithoutSim(ControllerInfo controllerInfo) {
        controllerInfo.setUsername("CTRLadmin");
        controllerInfo.setPassword("CTRLroot");
        controllerInfo.setAccountAccessKey("CTRLAccessKey");
        controllerInfo.setAccount("CTRLcustomer1");
        controllerInfo.setControllerHost("CTRLHost");
        controllerInfo.setControllerPort(9999);
        controllerInfo.setControllerSslEnabled(false);
        controllerInfo.setMachinePath("CTRLMachinePath|Something");
        controllerInfo.setSimEnabled(false);
        controllerInfo.setApplicationName("CTRLApp");
        controllerInfo.setTierName("CTRLTier");
        controllerInfo.setNodeName("CTRLNode");

        return controllerInfo;
    }

    private  Map valueMapWithoutSim(Map config) {
        config.put("enabled", "true");
        config.put("dashboardName", "testDashboardName");
        config.put("uploadDashboard", "true");
        config.put("executionFrequencyMinutes", "5");
        config.put("password", "root");
        config.put("username", "user");
        config.put("pathToNormalDashboard", "src/test/resources/dashboard/normalDashboard.json");
        return config;
    }


    @Test
    public void testReplacementOfSIMFields(){
        String json = getSIMJsonAsString();
        ControllerInfo controllerInfo = new ControllerInfo();
        controllerInfo = setUpControllerInfoWithSim(controllerInfo);

        Map config = new HashMap();
        config = valueMapWithSim(config);

        json = ReplaceDefaultInfo.replaceFields(json, controllerInfo,config);
        Assert.assertTrue(json.contains("testDashboardName"));
        Assert.assertTrue(json.contains("Root|CTRLMachinePath"));
        Assert.assertTrue(json.contains("Server & Infrastructure Monitoring"));
        Assert.assertTrue(json.contains("CTRLHost"));

        Assert.assertFalse(json.contains(REPLACE_HOST_NAME));
        Assert.assertFalse(json.contains(REPLACE_SIM_APPLICATION_NAME));
        Assert.assertFalse(json.contains(REPLACE_DASHBOARD_NAME));
        Assert.assertFalse(json.contains(REPLACE_MACHINE_PATH));

    }


    @Test
    public void testReplacementOfAppTierNodeFields(){
        String json = getAppTierNodeJsonAsString();
        ControllerInfo controllerInfo = new ControllerInfo();
        controllerInfo = setUpControllerInfoWithoutSim(controllerInfo);

        Map config = new HashMap();
        config = valueMapWithoutSim(config);

        json = ReplaceDefaultInfo.replaceFields(json, controllerInfo,config);
        Assert.assertTrue(json.contains("testDashboardName"));
        Assert.assertTrue(json.contains("CTRLApp"));
        Assert.assertTrue(json.contains("CTRLTier"));

        Assert.assertFalse(json.contains(REPLACE_HOST_NAME));
        Assert.assertFalse(json.contains(REPLACE_SIM_APPLICATION_NAME));
        Assert.assertFalse(json.contains(REPLACE_DASHBOARD_NAME));
        Assert.assertFalse(json.contains(REPLACE_TIER_NAME));
        Assert.assertFalse(json.contains(REPLACE_APPLICATION_NAME));
        Assert.assertFalse(json.contains(REPLACE_MACHINE_PATH));

    }
}
