/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.dashboard.SendDashboard;
import com.appdynamics.extensions.conf.ControllerInfo;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 7/17/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SendDashboard.class)
public class SendDashboardTest {

    //    private static final Logger logger = Logger.getLogger(SendDashboardTest.class);
    private org.slf4j.Logger logger = Mockito.mock(org.slf4j.Logger.class);

    private String dashboardJson;

    @Mock
    private ControllerInfo controllerInfo;


    private Map valueMapWithoutSim(Map config) {
        config.put("enabled", "true");
        config.put("dashboardName", "Prefix");
        config.put("uploadDashboard", "true");
        config.put("executionFrequencyMinutes", "5");
        config.put("host", "localhost");
        config.put("port", "8090");
        config.put("account", "customer");
        config.put("password", "root");
        config.put("username", "user");
        config.put("accountAccessKey", "pass");
        config.put("applicationName", "app");
        config.put("tierName", "tier");
        config.put("nodeName", "node");
        config.put("pathToNormalDashboard", "src/test/resources/dashboard/normalDashboard.json");
        return config;
    }

    private Map valueMapWithSim(Map config) {
        config.put("enabled", "true");
        config.put("dashboardName", "Prefix");
        config.put("uploadDashboard", "true");
        config.put("executionFrequencyMinutes", "5");
        config.put("host", "localhost");
        config.put("port", "8090");
        config.put("account", "customer");
        config.put("password", "root");
        config.put("username", "user");
        config.put("accountAccessKey", "pass");
        config.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");

        return config;
    }

    private void getJsonAsString() {
        try {
            dashboardJson = FileUtils.readFileToString(new File("src/test/resources/dashboard/simDashboard.json"));

        } catch (Exception e) {
            logger.error("Error in file reading");
        }
    }

    private ControllerInfo setUpControllerInfoWithoutSim(ControllerInfo controllerInfo) {
        controllerInfo.setUsername("admin");
        controllerInfo.setPassword("root");
        controllerInfo.setAccountAccessKey("keyisthisthing");
        controllerInfo.setAccount("customer1");
        controllerInfo.setControllerHost("Host");
        controllerInfo.setControllerPort(8090);
        controllerInfo.setControllerSslEnabled(false);
        controllerInfo.setMachinePath("MachinePath|Something");
        controllerInfo.setSimEnabled(false);
        controllerInfo.setApplicationName("App");
        controllerInfo.setTierName("Tier");
        controllerInfo.setNodeName("Node");

        return controllerInfo;
    }

    private ControllerInfo setUpControllerInfoWithSim(ControllerInfo controllerInfo) {
        controllerInfo.setUsername("admin");
        controllerInfo.setPassword("root");
        controllerInfo.setAccountAccessKey("keyisthisthing");
        controllerInfo.setAccount("customer1");
        controllerInfo.setControllerHost("Host");
        controllerInfo.setControllerPort(8090);
        controllerInfo.setControllerSslEnabled(false);
        controllerInfo.setMachinePath("MachinePath|Something");
        controllerInfo.setSimEnabled(true);
        return controllerInfo;
    }

    private ControllerInfo setUpControllerInfoWithoutUsernameAndPassword(ControllerInfo controllerInfo) {
        controllerInfo.setAccountAccessKey("keyisthisthing");
        controllerInfo.setAccount("customer1");
        controllerInfo.setControllerHost("Host");
        controllerInfo.setControllerPort(8090);
        controllerInfo.setControllerSslEnabled(false);
        controllerInfo.setMachinePath("MachinePath|Something");
        controllerInfo.setSimEnabled(true);
        return controllerInfo;
    }


    @Test
    public void testDashboardUploadWithoutSim() {

        ControllerInfo controllerInformation = new ControllerInfo();
        controllerInformation = setUpControllerInfoWithoutSim(controllerInformation);
        Map config = new HashMap();
        config = valueMapWithoutSim(config);
        getJsonAsString();
        CustomDashboardJsonUploader customDashboardJsonUploader = Mockito.mock(CustomDashboardJsonUploader.class);
        Mockito.doNothing().when(customDashboardJsonUploader).uploadDashboard(config.get("dashboardName").toString(), dashboardJson, config, false);
        Mockito.when(controllerInfo.getControllerInfo()).thenReturn(controllerInformation);
        try {
            SendDashboard dashboard = new SendDashboard(config, customDashboardJsonUploader, controllerInfo);
            dashboard.sendDashboard();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testDashboardUploadWithSim() {
        ControllerInfo controllerInformation = new ControllerInfo();
        controllerInformation = setUpControllerInfoWithSim(controllerInformation);

        Map config = new HashMap();
        config = valueMapWithSim(config);
        CustomDashboardJsonUploader customDashboardJsonUploader = Mockito.mock(CustomDashboardJsonUploader.class);
        Mockito.doNothing().when(customDashboardJsonUploader).uploadDashboard(config.get("dashboardName").toString(), dashboardJson, config, false);
        Mockito.when(controllerInfo.getControllerInfo()).thenReturn(controllerInformation);
        try {
            SendDashboard dashboard = new SendDashboard(config, customDashboardJsonUploader, controllerInfo);
            dashboard.sendDashboard();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testToCheckIfCredentialsFromConfigAreBeingUsed() {
        ControllerInfo controllerInformation = new ControllerInfo();
        controllerInformation = setUpControllerInfoWithoutUsernameAndPassword(controllerInformation);

        Map config = new HashMap();
        config = valueMapWithSim(config);
        CustomDashboardJsonUploader customDashboardJsonUploader = Mockito.mock(CustomDashboardJsonUploader.class);
        Mockito.doNothing().when(customDashboardJsonUploader).uploadDashboard(config.get("dashboardName").toString(), dashboardJson, config, false);
        Mockito.when(controllerInfo.getControllerInfo()).thenReturn(controllerInformation);
        try {
            SendDashboard dashboard = new SendDashboard(config, customDashboardJsonUploader, controllerInfo);
            dashboard.sendDashboard();
        } catch (Exception e) {
            Assert.fail();
        }
    }


//    @Test
//    public void testAppTierNodeNotConfiguredSIMNotEnabled() {
//        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
//
//        ControllerInfo controllerInformation = new ControllerInfo();
//        controllerInformation = setUpControllerInfoWithSim(controllerInformation);
//
//        Map config = new HashMap();
//        config = valueMapWithSim(config);
//        creatDashboardMap();
//
//        CustomDashboardJsonUploader customDashboardJsonUploader = Mockito.mock(CustomDashboardJsonUploader.class);
//        Mockito.doNothing().when(customDashboardJsonUploader).uploadDashboard(config.get("dashboardName").toString(), dashboardJson, config, false);
//        Mockito.when(controllerInfo.getControllerInfo()).thenReturn(controllerInformation);
//        try {
//            Dashboard dashboard = new Dashboard(config, dashboardMap, customDashboardJsonUploader, controllerInfo, logger);
//            dashboard.sendDashboard();
//        } catch (Exception e) {
//            Assert.fail();
//        }
//
//
////        Mockito.verify(logger, Mockito.times(2)).info(simLogArgsCaptor.capture(), simLogArgsCaptor.capture());
//
//        Mockito.verify(logger, Mockito.times(10)).debug(logCaptor.capture(), logCaptor.capture());
//
//        List<String> allValues1 = logCaptor.getAllValues();
//
//        String value = logCaptor.getValue();
//        Assert.assertTrue(value.contains("SIM is not enabled and Application name, Tier name or node name not configured."));
//    }


}
