package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * @author Satish Muddam
 */
public class MachineAgentAvailabilityCheckTest {

    private Logger logger = Mockito.mock(Logger.class);


    @Test
    public void testNullControllerInfo() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(null, null, logger);
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

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, null, logger);
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

        ControllerClient controllerClient = Mockito.mock(ControllerClient.class);
        Mockito.when(controllerClient.sendGetRequest(Matchers.anyString())).thenReturn(maStatusResponse(1));

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, controllerClient, logger);
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

        ControllerClient controllerClient = Mockito.mock(ControllerClient.class);
        Mockito.when(controllerClient.sendGetRequest(Matchers.anyString())).thenReturn(maStatusResponse(0));

        MachineAgentAvailabilityCheck machineAgentAvailabilityCheck = new MachineAgentAvailabilityCheck(controllerInfo, controllerClient, logger);
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
