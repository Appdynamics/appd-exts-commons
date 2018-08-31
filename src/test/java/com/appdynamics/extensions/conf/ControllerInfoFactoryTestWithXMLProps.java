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
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by bhuvnesh.kumar on 8/31/18.
 */
public class ControllerInfoFactoryTestWithXMLProps {

    @Test
    public void testGetControllerInfoWithXML() {
        Map mapForConfig = new HashMap();

        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(mapForConfig, file);

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
        Assert.assertTrue(controllerInfo.getUsername().equals("usernameYML"));
        Assert.assertTrue(controllerInfo.getPassword().equals("passwordYML"));

    }

    @Test
    public void testGetControllerInfoWithXMLandConfig() {
        Map config = getConfigMap();

        File file = new File("src/test/resources/dashboard/");
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config, file);

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


}
