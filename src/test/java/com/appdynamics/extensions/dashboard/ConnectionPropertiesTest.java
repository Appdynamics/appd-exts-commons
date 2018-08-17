/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.conf.ControllerInfoFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/8/18.
 */
public class ConnectionPropertiesTest {

//    private  Map valueMapWithoutSim(Map config) {
//        config.put("enabled", "true");
//        config.put("namePrefix", "Prefix");
//        config.put("uploadDashboard", "true");
//        config.put("executionFrequencyMinutes", "5");
//        config.put("password", "root");
//        config.put("username", "user");
//        config.put("applicationName", "app");
//        config.put("tierName", "tier");
//        config.put("nodeName", "node");
//        config.put("pathToNormalDashboard", "src/test/resources/dashboard/normalDashboard.json");
//        return config;
//    }
//
//    private  Map valueMapWithSim(Map config) {
//        config.put("enabled", "true");
//        config.put("namePrefix", "Prefix");
//        config.put("uploadDashboard", "true");
//        config.put("executionFrequencyMinutes", "5");
//        config.put("password", "root");
//        config.put("username", "user");
//        config.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");
//
//        return config;
//    }
//
//    private  Map valueMapWithSimAndNOUsernameAndPassword(Map config) {
//        config.put("enabled", "true");
//        config.put("namePrefix", "Prefix");
//        config.put("uploadDashboard", "true");
//        config.put("executionFrequencyMinutes", "5");
//        config.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");
//
//        return config;
//    }
//    private  ControllerInfo setUpControllerInfoWithoutSim(ControllerInfo controllerInfo) {
//        controllerInfo.setUsername("CTRLadmin");
//        controllerInfo.setPassword("CTRLroot");
//        controllerInfo.setAccountAccessKey("CTRLAccessKey");
//        controllerInfo.setAccount("CTRLcustomer1");
//        controllerInfo.setControllerHost("CTRLHost");
//        controllerInfo.setControllerPort(9999);
//        controllerInfo.setControllerSslEnabled(false);
//        controllerInfo.setMachinePath("CTRLMachinePath|Something");
//        controllerInfo.setSimEnabled(false);
//        controllerInfo.setApplicationName("CTRLApp");
//        controllerInfo.setTierName("CTRLTier");
//        controllerInfo.setNodeName("CTRLNode");
//
//        return controllerInfo;
//    }
//
//    private  ControllerInfo setUpControllerInfoWithSim(ControllerInfo controllerInfo) {
//        controllerInfo.setUsername("CTRLadmin");
//        controllerInfo.setPassword("CTRLroot");
//        controllerInfo.setAccountAccessKey("CTRLAccessKey");
//        controllerInfo.setAccount("CTRLcustomer1");
//        controllerInfo.setControllerHost("CTRLHost");
//        controllerInfo.setControllerPort(9999);
//        controllerInfo.setControllerSslEnabled(false);
//        controllerInfo.setMachinePath("CTRLMachinePath|Something");
//        controllerInfo.setSimEnabled(true);
//        return controllerInfo;
//    }
//
//    private  ControllerInfo setUpControllerInfoWithoutUsernameAndPassword(ControllerInfo controllerInfo) {
//        controllerInfo.setAccountAccessKey("CTRLAccessKey");
//        controllerInfo.setAccount("CTRLcustomer1");
//        controllerInfo.setControllerHost("CTRLHost");
//        controllerInfo.setControllerPort(9999);
//        controllerInfo.setControllerSslEnabled(false);
//        controllerInfo.setMachinePath("CTRLMachinePath|Something");
//        controllerInfo.setSimEnabled(true);
//        return controllerInfo;
//    }
//
//
//    @Test
//    public  void testCorrectValuesFromEnvironmentVariables(){
//        Map config = new HashMap();
//        config = valueMapWithSimAndNOUsernameAndPassword(config);
//
//        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config);
//        config = valueMapWithoutSim(config);
//        Map argsMap = new HashMap();
//        argsMap = ConnectionProperties.getArgumentMap(controllerInfo, config);
//
//        ArrayList arrayList = new ArrayList();
//        arrayList = (ArrayList)(argsMap.get("servers"));
//        Map data = (Map) arrayList.get(0);
//        Assert.assertTrue(data.get("password").toString().equals("CTRLroot"));
//        Assert.assertTrue(data.get("port").equals("9999"));
//        Assert.assertTrue(data.get("host").toString().equals("CTRLHost"));
//        Assert.assertTrue(data.get("username").equals("CTRLadmin@CTRLcustomer1"));
//        Assert.assertTrue(data.get("useSsl").toString().equals("false"));
//    }
//
//
//    @Test
//    public  void testCorrectValuesWithoutEnvironmentVariables(){
//        Map config = new HashMap();
//        config = valueMapWithSimAndNOUsernameAndPassword(config);
//
//        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config);
//        controllerInfo = setUpControllerInfoWithoutUsernameAndPassword(controllerInfo);
//        Map argsMap = new HashMap();
//        argsMap = ConnectionProperties.getArgumentMap(controllerInfo, config);
//
//        ArrayList arrayList = new ArrayList();
//        arrayList = (ArrayList)(argsMap.get("servers"));
//        Map data = (Map) arrayList.get(0);
//        Assert.assertTrue(data.get("password").toString().equals("root") );
//        Assert.assertTrue(data.get("port").equals("9999"));
//        Assert.assertTrue(data.get("host").toString().equals("CTRLHost"));
//        Assert.assertTrue(data.get("username").toString().equals("user@CTRLcustomer1"));
//        Assert.assertTrue(data.get("useSsl").toString().equals("false"));
//    }
//
//    @Test
//    public  void tesDefaultValuesReturnedWhenNothingProvidedForUserAndPass(){
//        Map config = new HashMap();
//        config = valueMapWithSimAndNOUsernameAndPassword(config);
//
//        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config);
//        controllerInfo = setUpControllerInfoWithoutUsernameAndPassword(controllerInfo);
//        Map argsMap = new HashMap();
//        argsMap = ConnectionProperties.getArgumentMap(controllerInfo, config);
//
//        ArrayList arrayList = new ArrayList();
//        arrayList = (ArrayList)(argsMap.get("servers"));
//        Map data = (Map) arrayList.get(0);
//        Assert.assertTrue(data.get("password").toString().equals("CTRLAccessKey") );
//        Assert.assertTrue(data.get("port").equals("9999"));
//        Assert.assertTrue(data.get("host").toString().equals("CTRLHost"));
//        Assert.assertTrue(data.get("username").toString().equals("singularity-agent@CTRLcustomer1"));
//        Assert.assertTrue(data.get("useSsl").toString().equals("false"));
//
//    }
}
