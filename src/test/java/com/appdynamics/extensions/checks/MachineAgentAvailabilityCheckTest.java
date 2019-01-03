package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.apiservices.ApplicationModelAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.IOException;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Satish Muddam
 */
public class MachineAgentAvailabilityCheckTest {

    private Logger logger = Mockito.mock(Logger.class);


    @Test
    public void testNullControllerInfo() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(null, metricAPIService, logger);
        machineAgentAvailabilityCheck.check();
        Mockito.verify(logger, Mockito.times(1)).error(logCaptor.capture());
        String value = logCaptor.getValue();
        Assert.assertEquals(value, "Received ControllerInfo as null. Not checking anything.");
    }

    @Test
    public void testAppTierNodeNotConfiguredSIMEnabled() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);

        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(true);
        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, metricAPIService, logger);
        machineAgentAvailabilityCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertTrue(value.contains("SIM is enabled, not checking MachineAgent availability metric"));
    }

    @Test
    public void testMAStatusAvailable() throws IOException, ControllerHttpRequestException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        when(metricAPIService.getMetricData(isA(String.class), isA(String.class))).thenReturn(new ObjectMapper().readTree(maStatusResponse(1)));

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, metricAPIService, logger);
        machineAgentAvailabilityCheck.check();

        Mockito.verify(logger, Mockito.times(2)).info(logCaptor.capture());

        String value = logCaptor.getAllValues().get(1);
        Assert.assertTrue(value.contains("MachineAgent is reporting availability metric"));
    }

    @Test
    public void testMAStatusNotAvailable() throws IOException, ControllerHttpRequestException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        ControllerInfo controllerInfo = Mockito.mock(ControllerInfo.class);
        Mockito.when(controllerInfo.getSimEnabled()).thenReturn(false);
        Mockito.when(controllerInfo.getApplicationName()).thenReturn("TestApp");
        Mockito.when(controllerInfo.getTierName()).thenReturn("TestTier");
        Mockito.when(controllerInfo.getNodeName()).thenReturn("TestNode");

        MetricAPIService metricAPIService = mock(MetricAPIService.class);
        when(metricAPIService.getMetricData(isA(String.class), isA(String.class))).thenReturn(new ObjectMapper().readTree(maStatusResponse(0)));

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, metricAPIService, logger);
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
                "    \"occurrences\": "+value+",\n" +
                "    \"current\": \"+value+\",\n" +
                "    \"min\": 2147483647,\n" +
                "    \"max\": -2147483648,\n" +
                "    \"startTimeInMillis\": 1508940780000,\n" +
                "    \"useRange\": false,\n" +
                "    \"count\": "+value+",\n" +
                "    \"sum\": "+value+",\n" +
                "    \"value\": "+value+",\n" +
                "    \"standardDeviation\": 0\n" +
                "  }]\n" +
                "}]s";
    }

}
