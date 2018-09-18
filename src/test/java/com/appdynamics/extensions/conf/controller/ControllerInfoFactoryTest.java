/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/29/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ControllerInfo.class)

public class ControllerInfoFactoryTest {

    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = ControllerInfoFactory.class.getDeclaredField("controllerInfo");
        instance.setAccessible(true);
        instance.set(null, null);
    }


    @Test
    public void testGetControllerInfoWithNoProps() {
        Map config = new HashMap();
        File file = Mockito.mock(File.class);
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

        Assert.assertTrue(controllerInfo.getAccount() == (null));
        Assert.assertTrue(controllerInfo.getAccountAccessKey() == (null));
        Assert.assertTrue(controllerInfo.getApplicationName() == (null));
        Assert.assertTrue(controllerInfo.getTierName() == (null));
        Assert.assertTrue(controllerInfo.getNodeName() == (null));
        Assert.assertTrue(controllerInfo.getControllerHost() == (null));
        Assert.assertTrue(controllerInfo.getControllerPort() == (null));
        Assert.assertTrue(controllerInfo.getUsername() == (null));
        Assert.assertTrue(controllerInfo.getPassword() == (null));
        Assert.assertTrue(controllerInfo.getEncryptionKey() == (null));
        Assert.assertTrue(controllerInfo.getEncryptedPassword() == (null));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled() == (null));
        Assert.assertTrue(controllerInfo.getUniqueHostId() == (null));
        Assert.assertTrue(controllerInfo.getSimEnabled() == (null));

    }

    @Test
    public void testGetControllerInfoWithConfigMap() {
        File file = Mockito.mock(File.class);
        Map config = getConfigMap();
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();


        Assert.assertTrue(controllerInfo.getAccount().equals("accountNameYML"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("accessKeyYML"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("applicationNameYML"));
        Assert.assertTrue(controllerInfo.getTierName().equals("tierNameYML"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("nodeNameYML"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("hostNameYML"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(9999));
        Assert.assertTrue(controllerInfo.getUsername().equals("usernameYML"));
        Assert.assertTrue(controllerInfo.getPassword().equals("passwordYML"));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("uniqueHostIDYML"));

    }


    @Test
    public void testGetControllerInfoWithXML() {
        Map config = new HashMap();
        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

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
    public void testGetControllerInfoWithXMLandConfig() {
        Map config = getConfigMap();

        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

        Assert.assertTrue(controllerInfo.getAccount().equals("accountNameYML"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("accessKeyYML"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("applicationNameYML"));
        Assert.assertTrue(controllerInfo.getTierName().equals("tierNameYML"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("nodeNameYML"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("hostNameYML"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(9999));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("uniqueHostIDYML"));
        Assert.assertTrue(controllerInfo.getSimEnabled().equals(false));

    }

    @Test
    public void testGetControllerInfoWithSystemProps() {

        setupSystemProps();
        Map config = null;
        File file = Mockito.mock(File.class);
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

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
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();


        Assert.assertTrue(controllerInfo.getUsername().equals("usernameYML"));
        Assert.assertTrue(controllerInfo.getPassword().equals("passwordYML"));
        Assert.assertTrue(controllerInfo.getEncryptionKey().equals("encryptionKey"));
        Assert.assertTrue(controllerInfo.getEncryptedPassword().equals("encryptedPassword"));

        Assert.assertTrue(controllerInfo.getAccount().equals("accountNameYML"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("accessKeyYML"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("applicationNameYML"));
        Assert.assertTrue(controllerInfo.getTierName().equals("tierNameYML"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("nodeNameYML"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("hostNameYML"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(9999));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("uniqueHostIDYML"));
        Assert.assertTrue(controllerInfo.getSimEnabled().equals(false));

    }

    @Test
    public void testGetControllerInfoWithSystemPropsAndXML() {
        setupSystemProps();
        Map config = new HashMap();
        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

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
        ControllerInfo controllerInfo ;
        ControllerInfoFactory.initialize(config, file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();

        Assert.assertTrue(controllerInfo.getUsername().equals("usernameYML"));
        Assert.assertTrue(controllerInfo.getPassword().equals("passwordYML"));
        Assert.assertTrue(controllerInfo.getEncryptionKey().equals("encryptionKey"));
        Assert.assertTrue(controllerInfo.getEncryptedPassword().equals("encryptedPassword"));

        Assert.assertTrue(controllerInfo.getAccount().equals("accountNameYML"));
        Assert.assertTrue(controllerInfo.getAccountAccessKey().equals("accessKeyYML"));
        Assert.assertTrue(controllerInfo.getApplicationName().equals("applicationNameYML"));
        Assert.assertTrue(controllerInfo.getTierName().equals("tierNameYML"));
        Assert.assertTrue(controllerInfo.getNodeName().equals("nodeNameYML"));
        Assert.assertTrue(controllerInfo.getControllerHost().equals("hostNameYML"));
        Assert.assertTrue(controllerInfo.getControllerPort().equals(9999));
        Assert.assertTrue(controllerInfo.getControllerSslEnabled().equals(false));
        Assert.assertTrue(controllerInfo.getUniqueHostId().equals("uniqueHostIDYML"));
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

        System.setProperty("appdynamics.agent.accountAccessKey", "accessKey");
        System.setProperty("appdynamics.agent.accountName", "accountName");
        System.setProperty("appdynamics.agent.applicationName", "applicationName");
        System.setProperty("appdynamics.agent.tierName", "tierName");
        System.setProperty("appdynamics.agent.nodeName", "nodeName");
        System.setProperty("appdynamics.controller.hostName", "hostName");
        System.setProperty("appdynamics.agent.monitors.controller.username", "username");
        System.setProperty("appdynamics.agent.monitors.controller.password", "password");
        System.setProperty("appdynamics.agent.monitors.controller.encryptionKey", "encryptionKey");
        System.setProperty("appdynamics.agent.monitors.controller.encryptedPassword", "encryptedPassword");
        System.setProperty("appdynamics.controller.port", "9090");
        System.setProperty("appdynamics.controller.ssl.enabled", "false");
        System.setProperty("appdynamics.agent.uniqueHostId", "uniqueHostID");
        System.setProperty("appdynamics.sim.enabled", "false");

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