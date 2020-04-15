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
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
public class MachineAgentAvailabilityCheckTest {

    @Mock
    private org.slf4j.Logger logger;


    public void setup() {
        PowerMockito.mockStatic(ExtensionsLoggerFactory.class);
        Mockito.when(ExtensionsLoggerFactory.getLogger(MachineAgentAvailabilityCheck.class)).thenReturn(logger);
    }


    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testNullControllerInfo() {

        setup();

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        when(controllerAPIService.getMetricAPIService()).thenReturn(metricAPIService);
        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(null, controllerAPIService, 5, TimeUnit.SECONDS);
        machineAgentAvailabilityCheck.check();
        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());
        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Received ControllerInfo as null. Not checking anything.");
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testAppTierNodeNotConfiguredSIMEnabled() {

        setup();

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);

        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);
        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        when(controllerAPIService.getMetricAPIService()).thenReturn(metricAPIService);
        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, controllerAPIService, 5, TimeUnit.SECONDS);
        machineAgentAvailabilityCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertTrue(value.contains("SIM is enabled, not checking MachineAgent availability metric"));
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMAStatusAvailable() throws IOException {

        setup();

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        when(controllerAPIService.getMetricAPIService()).thenReturn(metricAPIService);
        when(metricAPIService.getMetricData(isA(String.class), isA(String.class))).thenReturn(new ObjectMapper().readTree(maStatusResponse(1)));

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, controllerAPIService, 5, TimeUnit.SECONDS);
        machineAgentAvailabilityCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertTrue(value.contains("MachineAgent is reporting availability metric"));
    }

    @Test
    @PrepareForTest(ExtensionsLoggerFactory.class)
    public void testMAStatusNotAvailable() throws IOException {

        setup();

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        when(controllerAPIService.getMetricAPIService()).thenReturn(metricAPIService);
        when(metricAPIService.getMetricData(isA(String.class), isA(String.class))).thenReturn(new ObjectMapper().readTree(maStatusResponse(0)));

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, controllerAPIService, 5, TimeUnit.SECONDS);
        machineAgentAvailabilityCheck.check();

        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());

        String value = logCaptor.getValue();
        Assert.assertTrue(value.contains("MachineAgent is not reporting availability metric. Please check your configuration"));
    }

    private String maStatusResponse(int value) {
        return "[{\n" +
                "  \"metricName\": \"Agent|Machine|Availability\",\n" +
                "  \"metricId\": 210482,\n" +
                "  \"metricPath\": \"Application Infrastructure Performance|mytier|Agent|Machine|Availability\",\n" +
                "  \"frequency\": \"ONE_MIN\",\n" +
                "  \"metricValues\": [  {\n" +
                "    \"occurrences\": " + value + ",\n" +
                "    \"current\": \"+value+\",\n" +
                "    \"min\": 2147483647,\n" +
                "    \"max\": -2147483648,\n" +
                "    \"startTimeInMillis\": 1508940780000,\n" +
                "    \"useRange\": false,\n" +
                "    \"count\": " + value + ",\n" +
                "    \"sum\": " + value + ",\n" +
                "    \"value\": " + value + ",\n" +
                "    \"standardDeviation\": 0\n" +
                "  }]\n" +
                "}]s";
    }

}
