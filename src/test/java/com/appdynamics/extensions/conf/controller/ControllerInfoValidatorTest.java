/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.controller;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by bhuvnesh.kumar on 8/29/18.
 */

public class ControllerInfoValidatorTest {
    @Test
    public void validateAndCheckIfResolvedTestWithAppTierNode() throws Exception {

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
        Boolean check = controllerInfoValidator.validateAndCheckIfResolved(controllerInfo);
        Assert.assertTrue(check);
    }


    @Test
    public void validateAndCheckIfResolvedTestWithOnlySim() throws Exception {

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
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);

        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator();
        Boolean check = controllerInfoValidator.validateAndCheckIfResolved(controllerInfo);

        Assert.assertTrue(check);
    }

    @Test
    public void validateAndCheckIfResolvedTestWithSimAndAppTierNode() throws Exception {

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
        Boolean check = controllerInfoValidator.validateAndCheckIfResolved(controllerInfo);
        Assert.assertTrue(check);
    }

    @Test
    public void validateAndCheckIfResolvedTestWithoutSimAndNode() throws Exception {

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
        Boolean check = controllerInfoValidator.validateAndCheckIfResolved(controllerInfo);
        Assert.assertFalse(check);
    }

}