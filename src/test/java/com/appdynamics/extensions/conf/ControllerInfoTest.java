/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/29/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ControllerInfo.class)

public class ControllerInfoTest {


    @Test
    public void fromXMLTest() {
        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo = ControllerInfo.getControllerInfoFromXml(file);

        Assert.assertTrue(controllerInfo.getAccount().equals("xmlAccountName"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("xmlAccessKey"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("xmlApplicationName"));
        Assert.assertTrue(controllerInfo.getTierName().equals("xmlTierName"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("xmlNodeName"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("xmlHost"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(8090));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("xmlUniqueHostId"));
        Assert.assertTrue(controllerInfo.getSimEnabled().equals(false));
    }

    @Test
    public void testFromSystemProperties() throws Exception {

        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getProperty("appdynamics.agent.accountAccessKey")).thenReturn("accessKey");
        PowerMockito.when(System.getProperty("appdynamics.agent.accountName")).thenReturn("accountName");
        PowerMockito.when(System.getProperty("appdynamics.agent.applicationName")).thenReturn("applicationName");
        PowerMockito.when(System.getProperty("appdynamics.agent.tierName")).thenReturn("tierName");
        PowerMockito.when(System.getProperty("appdynamics.agent.nodeName")).thenReturn("nodeName");
        PowerMockito.when(System.getProperty("appdynamics.controller.hostName")).thenReturn("hostName");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.username")).thenReturn("username");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.password")).thenReturn("password");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.encryptionKey")).thenReturn("encryptionKey");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword")).thenReturn("encryptedPassword");
        PowerMockito.when(System.getProperty("appdynamics.controller.port")).thenReturn("9090");
        PowerMockito.when(System.getProperty("appdynamics.controller.ssl.enabled")).thenReturn("false");
        PowerMockito.when(System.getProperty("appdynamics.agent.uniqueHostId")).thenReturn("uniqueHostID");
        PowerMockito.when(System.getProperty("appdynamics.sim.enabled")).thenReturn("false");

        ControllerInfo controllerInfo = ControllerInfo.fromSystemProperties();

        Assert.assertTrue(controllerInfo.getAccount().equals("accountName"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("accessKey"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("applicationName"));
        Assert.assertTrue(controllerInfo.getTierName().equals("tierName"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("nodeName"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("hostName"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(9090));
        Assert.assertTrue(controllerInfo.getUsername().equals("username"));
        Assert.assertTrue(controllerInfo.getPassword().equals("password"));
        Assert.assertTrue(controllerInfo.getEncryptionKey().equals("encryptionKey"));
        Assert.assertTrue(controllerInfo.getEncryptedPassword().equals("encryptedPassword"));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("uniqueHostID"));
        Assert.assertTrue(controllerInfo.getSimEnabled().equals(false));
    }

    @Test
    public void testFromYml() {
        Map config = new HashMap<>();
        config.put("controllerHost", "hostName");
        config.put("controllerPort", 9090);
        config.put("controllerSslEnabled", false);
        config.put("uniqueHostId", "uniqueHostID");
        config.put("account", "accountName");
        config.put("username", "username");
        config.put("password", "password");
        config.put("accountAccessKey", "accessKey");
        config.put("applicationName", "applicationName");
        config.put("tierName", "tierName");
        config.put("nodeName", "nodeName");

        ControllerInfo controllerInfo = ControllerInfo.fromYml(config);

        Assert.assertTrue(controllerInfo.getAccount().equals("accountName"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("accessKey"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("applicationName"));
        Assert.assertTrue(controllerInfo.getTierName().equals("tierName"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("nodeName"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("hostName"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(9090));
        Assert.assertTrue(controllerInfo.getUsername().equals("username"));
        Assert.assertTrue(controllerInfo.getPassword().equals("password"));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("uniqueHostID"));

    }

    @Test
    public void testMerge() throws Exception {

        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getProperty("appdynamics.agent.accountAccessKey")).thenReturn("accessKey");
        PowerMockito.when(System.getProperty("appdynamics.agent.accountName")).thenReturn("accountName");
        PowerMockito.when(System.getProperty("appdynamics.agent.applicationName")).thenReturn("applicationName");
        PowerMockito.when(System.getProperty("appdynamics.agent.tierName")).thenReturn("tierName");
        PowerMockito.when(System.getProperty("appdynamics.agent.nodeName")).thenReturn("nodeName");
        PowerMockito.when(System.getProperty("appdynamics.controller.hostName")).thenReturn("hostName");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.username")).thenReturn("username");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.password")).thenReturn("password");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.encryptionKey")).thenReturn("encryptionKey");
        PowerMockito.when(System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword")).thenReturn("encryptedPassword");
        PowerMockito.when(System.getProperty("appdynamics.controller.port")).thenReturn("9090");
        PowerMockito.when(System.getProperty("appdynamics.controller.ssl.enabled")).thenReturn("false");
        PowerMockito.when(System.getProperty("appdynamics.agent.uniqueHostId")).thenReturn("uniqueHostID");
        PowerMockito.when(System.getProperty("appdynamics.sim.enabled")).thenReturn("false");

        ControllerInfo systemProperties = ControllerInfo.fromSystemProperties();

        Map configMap = new HashMap();
        configMap.put("controllerHost", "hostNameYML");
        configMap.put("controllerPort", 9999);
        configMap.put("controllerSslEnabled", true);
        configMap.put("uniqueHostId", "uniqueHostIDYML");
        configMap.put("account", "accountNameYML");
        configMap.put("username", "usernameYML");
        configMap.put("password", "passwordYML");
        configMap.put("accountAccessKey", "accessKeyYML");
        configMap.put("applicationName", "applicationNameYML");
        configMap.put("tierName", "tierNameYML");
        configMap.put("nodeName", "nodeNameYML");

        ControllerInfo configProperties = ControllerInfo.fromYml(configMap);

        ControllerInfo mergeTest = configProperties.merge(systemProperties);

        Assert.assertTrue(true);

        Assert.assertTrue(mergeTest.getAccount().equals("accountName"));
        Assert.assertTrue(mergeTest.getAccountAccessKey().equals("accessKey"));
        Assert.assertTrue(mergeTest.getApplicationName().equals("applicationName"));
        Assert.assertTrue(mergeTest.getTierName().equals("tierName"));
        Assert.assertTrue(mergeTest.getNodeName().equals("nodeName"));
        Assert.assertTrue(mergeTest.getControllerHost().equals("hostName"));
        Assert.assertTrue(mergeTest.getControllerPort().equals(9090));
        Assert.assertTrue(mergeTest.getUsername().equals("username"));
        Assert.assertTrue(mergeTest.getPassword().equals("password"));
        Assert.assertTrue(mergeTest.getControllerSslEnabled().equals(false));
        Assert.assertTrue(mergeTest.getUniqueHostId().equals("uniqueHostID"));


    }


}