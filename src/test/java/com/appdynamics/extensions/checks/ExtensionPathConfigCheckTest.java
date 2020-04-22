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
import com.appdynamics.extensions.controller.apiservices.ApplicationModelAPIService;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
public class ExtensionPathConfigCheckTest {

    @Mock
    private org.slf4j.Logger logger;


    @Before
    public void setup() {
        PowerMockito.mockStatic(ExtensionsLoggerFactory.class);
        Mockito.when(ExtensionsLoggerFactory.getLogger(ExtensionPathConfigCheck.class)).thenReturn(logger);
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testNullControllerInfo() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        ApplicationModelAPIService applicationModelAPIService = mock(ApplicationModelAPIService.class);
        when(controllerAPIService.getApplicationModelAPIService()).thenReturn(applicationModelAPIService);
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck("Custom Metrics|Test", null, controllerAPIService);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Received ControllerInfo as null. Not checking anything.");
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMetricPrefixWithComponentAndSIMEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);

        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        ApplicationModelAPIService applicationModelAPIService = mock(ApplicationModelAPIService.class);
        when(controllerAPIService.getApplicationModelAPIService()).thenReturn(applicationModelAPIService);
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck("Server|Component:Test|Custom Metrics|Test", controllerInfo, controllerAPIService);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "No need to configure tier-id as SIM is enabled. Please use the alternate metric prefix.");
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMetricPrefixSIMNotEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        ApplicationModelAPIService applicationModelAPIService = mock(ApplicationModelAPIService.class);
        when(controllerAPIService.getApplicationModelAPIService()).thenReturn(applicationModelAPIService);
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck("Custom Metrics|Test", controllerInfo, controllerAPIService);
        extensionPathConfigCheck.check();
        Mockito.verify(logger, Mockito.times(1)).warn(logCaptor.capture());
        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Configured metric prefix with no tier id. With this configuration, metric browser will show metric names in all the available tiers/applications(when there are multiple app agents)");
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMetricPrefixSIMEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);

        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        ApplicationModelAPIService applicationModelAPIService = mock(ApplicationModelAPIService.class);
        when(controllerAPIService.getApplicationModelAPIService()).thenReturn(applicationModelAPIService);
        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck("Custom Metrics|Test", controllerInfo, controllerAPIService);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertEquals(value, "SIM is enabled, please look in the SIM metric browser for metrics.");
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMetricPrefixWithComponentAndSIMNotEnabled() throws IOException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        ApplicationModelAPIService applicationModelAPIService = mock(ApplicationModelAPIService.class);
        when(controllerAPIService.getApplicationModelAPIService()).thenReturn(applicationModelAPIService);
        when(applicationModelAPIService.getSpecificTierNode(isA(String.class), isA(String.class))).thenReturn(new ObjectMapper().readTree(tierRESTResponse()));

        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck("Server|Component:TestTier|Custom Metrics|Test", controllerInfo, controllerAPIService);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertEquals(value, "Extension configured correct tier id/tier name");
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMetricPrefixWithWrongComponentAndSIMNotEnabled() throws IOException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        ApplicationModelAPIService applicationModelAPIService = mock(ApplicationModelAPIService.class);
        when(controllerAPIService.getApplicationModelAPIService()).thenReturn(applicationModelAPIService);
        when(applicationModelAPIService.getSpecificTierNode(isA(String.class), isA(String.class))).thenReturn(new ObjectMapper().readTree(tierRESTResponse()));


        ExtensionPathConfigCheck extensionPathConfigCheck = new ExtensionPathConfigCheck("Server|Component:TestTier123|Custom Metrics|Test", controllerInfo, controllerAPIService);
        extensionPathConfigCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Extension did not configure correct tier. Tier to configure [" + controllerInfo.getTierName() +
                "] with tier id [19], but configured tier [TestTier123]");
    }

    private String tierRESTResponse() {
        return "[{\"agentType\": \"APP_AGENT\", \"name\": \"mytier\", \"description\": \"\", \"id\": 19, \"numberOfNodes\": 1, \"type\": \"Application Server\" }]";
    }
}
