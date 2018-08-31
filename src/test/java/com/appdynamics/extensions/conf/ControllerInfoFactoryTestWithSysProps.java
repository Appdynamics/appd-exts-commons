/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/29/18.
 */

public class ControllerInfoFactoryTestWithSysProps {


    @Test
    public void testGetControllerInfoWithSystemProps() {

        setupSystemProps();
        File cxml = new File("");
        Map maping = new HashMap();
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(maping, cxml);
        removeSystemProperties();
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
    public void testGetControllerInfoWithSystemPropsAndConfig() {

        setupSystemProps();
        Map config = getConfigMap();

        File file = Mockito.mock(File.class);
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config, file);
        removeSystemProperties();
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
    public void testGetControllerInfoWithSystemPropsAndXML(){
        setupSystemProps();
        Map mapForConfig = new HashMap();
        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(mapForConfig, file);
        removeSystemProperties();
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
        Assert.assertTrue(controllerInfo.getSimEnabled().equals(false));


    }

    @Test
    public void testGetControllerInfoWithSystemPropsXmlConfig() {

        setupSystemProps();
        Map config = getConfigMap();

        File file = new File("src/test/resources/dashboard/");

        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config, file);
        removeSystemProperties();
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
        return config;
    }

    public void setupSystemProps() {

        System.setProperty("appdynamics.agent.accountAccessKey","accessKey");
        System.setProperty("appdynamics.agent.accountName","accountName");
        System.setProperty("appdynamics.agent.applicationName","applicationName");
        System.setProperty("appdynamics.agent.tierName","tierName");
        System.setProperty("appdynamics.agent.nodeName","nodeName");
        System.setProperty("appdynamics.controller.hostName","hostName");
        System.setProperty("appdynamics.agent.monitors.controller.username","username");
        System.setProperty("appdynamics.agent.monitors.controller.password","password");
        System.setProperty("appdynamics.agent.monitors.controller.encryptionKey","encryptionKey");
        System.setProperty("appdynamics.agent.monitors.controller.encryptedPassword","encryptedPassword");
        System.setProperty("appdynamics.controller.port","9090");
        System.setProperty("appdynamics.controller.ssl.enabled","false");
        System.setProperty("appdynamics.agent.uniqueHostId","uniqueHostID");
        System.setProperty("appdynamics.sim.enabled","false");

    }


    public void removeSystemProperties() {

        System.clearProperty("appdynamics.agent.accountAccessKey");
        System.clearProperty("appdynamics.agent.accountName");
        System.clearProperty("appdynamics.agent.applicationName");
        System.clearProperty("appdynamics.agent.tierName");
        System.clearProperty("appdynamics.agent.nodeName");
        System.clearProperty("appdynamics.controller.hostName");
        System.clearProperty("appdynamics.agent.monitors.controller.username");
        System.clearProperty("appdynamics.agent.monitors.controller.password");
        System.clearProperty("appdynamics.agent.monitors.controller.encryptionKey");
        System.clearProperty("appdynamics.agent.monitors.controller.encryptedPassword");
        System.clearProperty("appdynamics.controller.port");
        System.clearProperty("appdynamics.controller.ssl.enabled");
        System.clearProperty("appdynamics.agent.uniqueHostId");
        System.clearProperty("appdynamics.sim.enabled");


    }




}