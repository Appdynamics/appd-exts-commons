/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        Boolean check = controllerInfoValidator.isValidated();
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
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        Boolean check = controllerInfoValidator.isValidated();
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
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        Boolean check = controllerInfoValidator.isValidated();
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
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        Boolean check = controllerInfoValidator.isValidated();
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
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        Boolean check = controllerInfoValidator.isValidated();
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
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        Boolean check = controllerInfoValidator.isValidated();
        Assert.assertFalse(check);
    }
}