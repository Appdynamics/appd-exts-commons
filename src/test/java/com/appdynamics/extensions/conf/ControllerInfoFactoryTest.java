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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 8/29/18.
 */

// TODO test to verify the correct ControllerInfoFactory properties
@RunWith(PowerMockRunner.class)
@PrepareForTest(ControllerInfo.class)

public class ControllerInfoFactoryTest {


    @Test
    public void testGetControllerInfo() throws Exception {
        ControllerInfo system = Mockito.mock(ControllerInfo.class);
        Mockito.when(system.getAccount()).thenReturn("accountName");
        Mockito.when(system.getAccountAccessKey()).thenReturn("accessKey");
        Mockito.when(system.getApplicationName()).thenReturn("applicationName");
        Mockito.when(system.getTierName()).thenReturn("tierName");
        Mockito.when(system.getNodeName()).thenReturn("nodeName");
        Mockito.when(system.getControllerHost()).thenReturn("hostName");
        Mockito.when(system.getControllerPort()).thenReturn(9090);
        Mockito.when(system.getUsername()).thenReturn("username");
        Mockito.when(system.getPassword()).thenReturn("password");
        Mockito.when(system.getEncryptionKey()).thenReturn("encryptionKey");
        Mockito.when(system.getEncryptedPassword()).thenReturn("encryptedPassword");
        Mockito.when(system.getControllerSslEnabled()).thenReturn(false);
        Mockito.when(system.getUniqueHostId()).thenReturn("accountName");
        Mockito.when(system.getSimEnabled()).thenReturn(false);

        ControllerInfo xmlInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(xmlInfo.getAccount()).thenReturn("accountNameXml");
        Mockito.when(xmlInfo.getAccountAccessKey()).thenReturn("accessKeyXml");
        Mockito.when(xmlInfo.getApplicationName()).thenReturn("applicationNameXml");
        Mockito.when(xmlInfo.getTierName()).thenReturn("tierNameXml");
        Mockito.when(xmlInfo.getNodeName()).thenReturn("nodeNameXml");
        Mockito.when(xmlInfo.getControllerHost()).thenReturn("hostNameXml");
        Mockito.when(xmlInfo.getControllerPort()).thenReturn(9988);
        Mockito.when(xmlInfo.getUsername()).thenReturn("usernameXml");
        Mockito.when(xmlInfo.getPassword()).thenReturn("passwordXml");
        Mockito.when(xmlInfo.getEncryptionKey()).thenReturn("encryptionKeyXml");
        Mockito.when(xmlInfo.getEncryptedPassword()).thenReturn("encryptedPasswordXml");
        Mockito.when(xmlInfo.getControllerSslEnabled()).thenReturn(true);
        Mockito.when(xmlInfo.getUniqueHostId()).thenReturn("accountNameXml");
        Mockito.when(xmlInfo.getSimEnabled()).thenReturn(true);

        ControllerInfo config = Mockito.mock(ControllerInfo.class);
        Mockito.when(config.getAccount()).thenReturn("accountNameConfig");
        Mockito.when(config.getAccountAccessKey()).thenReturn("accessKeyConfig");
        Mockito.when(config.getApplicationName()).thenReturn("applicationNameConfig");
        Mockito.when(config.getTierName()).thenReturn("tierNameConfig");
        Mockito.when(config.getNodeName()).thenReturn("nodeNameConfig");
        Mockito.when(config.getControllerHost()).thenReturn("hostNameConfig");
        Mockito.when(config.getControllerPort()).thenReturn(8888);
        Mockito.when(config.getUsername()).thenReturn("usernameConfig");
        Mockito.when(config.getPassword()).thenReturn("passwordConfig");
        Mockito.when(config.getEncryptionKey()).thenReturn("encryptionKeyConfig");
        Mockito.when(config.getEncryptedPassword()).thenReturn("encryptedPasswordConfig");
        Mockito.when(config.getControllerSslEnabled()).thenReturn(true);
        Mockito.when(config.getUniqueHostId()).thenReturn("accountNameConfig");
        Mockito.when(config.getSimEnabled()).thenReturn(false);


        Mockito.mock(ControllerInfo.class);
        Mockito.when(ControllerInfo.fromSystemProperties()).thenReturn(system);
        Mockito.when(system.fromYml(Mockito.anyMap())).thenReturn(config);
        Mockito.when(system.getControllerInfoFromXml()).thenReturn(xmlInfo);

        ControllerInfo finalCInfo = ControllerInfoFactory.getControllerInfo(Mockito.anyMap());

        Assert.assertTrue(true);


    }
}