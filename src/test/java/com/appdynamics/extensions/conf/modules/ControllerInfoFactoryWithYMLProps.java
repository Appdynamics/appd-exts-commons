/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.conf.ControllerInfoFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/31/18.
 */
public class ControllerInfoFactoryWithYMLProps {

    @Test
    public void testGetControllerInfoWithConfigMap() {
        Map config = getConfigMap();

        File file = Mockito.mock(File.class);
        ControllerInfo controllerInfo = ControllerInfoFactory.getControllerInfo(config, file);

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
