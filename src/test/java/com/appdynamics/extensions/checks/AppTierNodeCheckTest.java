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

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.controller.ControllerInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.List;

/**
 * @author Satish Muddam
 */
public class AppTierNodeCheckTest {

    private Logger logger = Mockito.mock(Logger.class);


    @Test
    public void testNullControllerInfo() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        AppTierNodeCheck appTierNodeCheck = new AppTierNodeCheck(null, logger);
        appTierNodeCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Received ControllerInfo as null. Not checking anything.");
    }

    @Test
    public void testAppTierNodeNotConfiguredSIMNotEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);

        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);

        AppTierNodeCheck appTierNodeCheck = new AppTierNodeCheck(controllerInfo, logger);
        appTierNodeCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertTrue(value.contains("SIM is not enabled and Application name, Tier name or node name not resolved"));
    }

    @Test
    public void testAppTierNodeNotConfiguredSIMEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);

        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);

        AppTierNodeCheck appTierNodeCheck = new AppTierNodeCheck(controllerInfo, logger);
        appTierNodeCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertTrue(value.contains("Application name, Tier name or node name not configured but SIM is enabled"));
    }

    @Test
    public void testAppTierNodeConfiguredSIMNotEnabled() {
        ArgumentCaptor<String> logArgsCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);

        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");


        AppTierNodeCheck appTierNodeCheck = new AppTierNodeCheck(controllerInfo, logger);
        appTierNodeCheck.check();

        Mockito.verify(logger, Mockito.times(1)).info(logArgsCaptor.capture(), logArgsCaptor.capture(), logArgsCaptor.capture(), logArgsCaptor.capture());

        List<String> allValues = logArgsCaptor.getAllValues();
        Assert.assertEquals(allValues.get(0), "Application name [{}], Tier name [{}] and node name [{}] are configured");
        Assert.assertEquals(allValues.get(1), "TestApp");
        Assert.assertEquals(allValues.get(2), "TestTier");
        Assert.assertEquals(allValues.get(3), "TestNode");

        ArgumentCaptor<String> simLogArgsCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(logger, Mockito.times(2)).info(simLogArgsCaptor.capture(), simLogArgsCaptor.capture());
        List<String> allValues1 = simLogArgsCaptor.getAllValues();
        Assert.assertEquals(allValues1.get(0), "SIM is [{}]");
        Assert.assertEquals(allValues1.get(1), " Not Enabled ");
    }

    @Test
    public void testAppTierNodeConfiguredSIMEnabled() {
        ArgumentCaptor<String> logArgsCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);

        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");


        AppTierNodeCheck appTierNodeCheck = new AppTierNodeCheck(controllerInfo, logger);
        appTierNodeCheck.check();

        Mockito.verify(logger, Mockito.times(1)).info(logArgsCaptor.capture(), logArgsCaptor.capture(), logArgsCaptor.capture(), logArgsCaptor.capture());

        List<String> allValues = logArgsCaptor.getAllValues();
        Assert.assertEquals(allValues.get(0), "Application name [{}], Tier name [{}] and node name [{}] are configured");
        Assert.assertEquals(allValues.get(1), "TestApp");
        Assert.assertEquals(allValues.get(2), "TestTier");
        Assert.assertEquals(allValues.get(3), "TestNode");

        ArgumentCaptor<String> simLogArgsCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(logger, Mockito.times(2)).info(simLogArgsCaptor.capture(), simLogArgsCaptor.capture());
        List<String> allValues1 = simLogArgsCaptor.getAllValues();
        Assert.assertEquals(allValues1.get(0), "SIM is [{}]");
        Assert.assertEquals(allValues1.get(1), " Enabled ");
    }
}
