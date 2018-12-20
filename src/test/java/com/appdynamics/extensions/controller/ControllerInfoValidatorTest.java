/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.controller;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by bhuvnesh.kumar on 8/29/18.
 */

public class ControllerInfoValidatorTest {
    @Test
    public void testValidateAndCheckIfResolvedWhenAppTierNodePresentAndSimDisabled() throws Exception {
        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getUsername()).thenReturn("username");
        Mockito.when(controllerInfo.getPassword()).thenReturn("password");
        Mockito.when(controllerInfo.getAccount()).thenReturn("account");
        Mockito.when(controllerInfo.getControllerHost()).thenReturn("controllerHost");
        Mockito.when(controllerInfo.getControllerPort()).thenReturn(9080);
        Mockito.when(controllerInfo.getControllerSslEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("application");
        Mockito.when(controllerInfo.getTierName()).thenReturn("tier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("node");
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.isValidatedAndResolved(controllerInfo);
        Assert.assertTrue(check);
    }

    @Test
    public void testValidateAndCheckIfResolvedTestWhenSimDisabledAndNodeAbsent() throws Exception {
        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getUsername()).thenReturn("username");
        Mockito.when(controllerInfo.getPassword()).thenReturn("password");
        Mockito.when(controllerInfo.getAccount()).thenReturn("account");
        Mockito.when(controllerInfo.getControllerHost()).thenReturn("controllerHost");
        Mockito.when(controllerInfo.getControllerPort()).thenReturn(9080);
        Mockito.when(controllerInfo.getControllerSslEnabled()).thenReturn(true);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("application");
        Mockito.when(controllerInfo.getTierName()).thenReturn("tier");
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.isValidatedAndResolved(controllerInfo);
        Assert.assertFalse(check);
    }

    @Test
    public void testValidateAndCheckIfResolvedTestWhenSimEnabled() throws Exception {
        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getUsername()).thenReturn("username");
        Mockito.when(controllerInfo.getPassword()).thenReturn("password");
        Mockito.when(controllerInfo.getAccount()).thenReturn("account");
        Mockito.when(controllerInfo.getControllerHost()).thenReturn("controllerHost");
        Mockito.when(controllerInfo.getControllerPort()).thenReturn(9080);
        Mockito.when(controllerInfo.getControllerSslEnabled()).thenReturn(true);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.isValidatedAndResolved(controllerInfo);
        Assert.assertTrue(check);
    }

    @Test
    public void testValidateAndCheckIfResolvedTestWhenSimTrueAndAppTierNode() throws Exception {
        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getUsername()).thenReturn("username");
        Mockito.when(controllerInfo.getPassword()).thenReturn("password");
        Mockito.when(controllerInfo.getAccount()).thenReturn("account");
        Mockito.when(controllerInfo.getControllerHost()).thenReturn("controllerHost");
        Mockito.when(controllerInfo.getControllerPort()).thenReturn(9080);
        Mockito.when(controllerInfo.getControllerSslEnabled()).thenReturn(true);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("application");
        Mockito.when(controllerInfo.getTierName()).thenReturn("tier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("node");
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.isValidatedAndResolved(controllerInfo);
        Assert.assertTrue(check);
    }

    @Test
    public void testValidateAndCheckIfResolvedTestWhenSimAndAppTierNodeNotPresent() throws Exception {
        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getUsername()).thenReturn("username");
        Mockito.when(controllerInfo.getPassword()).thenReturn("password");
        Mockito.when(controllerInfo.getAccount()).thenReturn("account");
        Mockito.when(controllerInfo.getControllerHost()).thenReturn("controllerHost");
        Mockito.when(controllerInfo.getControllerPort()).thenReturn(9080);
        Mockito.when(controllerInfo.getControllerSslEnabled()).thenReturn(true);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.isValidatedAndResolved(controllerInfo);
        Assert.assertFalse(check);
    }

    @Test
    public void testValidatedAndResolvedTestWhenSIMEnabledIsNull() throws Exception {
        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getUsername()).thenReturn("username");
        Mockito.when(controllerInfo.getPassword()).thenReturn("password");
        Mockito.when(controllerInfo.getAccount()).thenReturn("account");
        Mockito.when(controllerInfo.getControllerHost()).thenReturn("controllerHost");
        Mockito.when(controllerInfo.getControllerPort()).thenReturn(9080);
        Mockito.when(controllerInfo.getControllerSslEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("application");
        Mockito.when(controllerInfo.getTierName()).thenReturn("tier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("node");
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(null);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.isValidatedAndResolved(controllerInfo);
        Assert.assertFalse(check);
    }
}