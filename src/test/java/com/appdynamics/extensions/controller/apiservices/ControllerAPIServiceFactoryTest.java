package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 1/2/19.
 */

public class ControllerAPIServiceFactoryTest {
    @Test
    public void whenControllerClientIsValidAndHTTPCallSuccessfulShouldReturnValidData() throws ControllerHttpRequestException{
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        ControllerClient controllerClient = mock(ControllerClient.class);
        when(controllerClient.sendGetRequest(isA(String.class))).thenReturn("{\"key\": \"1\"}");
        ControllerAPIService controllerAPIService = ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
        MetricAPIService metricAPIService = controllerAPIService.getMetricAPIService();
        Assert.assertNotNull(metricAPIService);
        JsonNode jsonNode = metricAPIService.getMetricData("applicationName", "endPoint");
        Assert.assertEquals(jsonNode.get("key").asText(), "1");
    }

    @Test
    public void whenControllerClientIsNullShouldReturnNull() throws ControllerHttpRequestException{
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        ControllerClient controllerClient = null;
        ControllerAPIService controllerAPIService = ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
        MetricAPIService metricAPIService = controllerAPIService.getMetricAPIService();
        Assert.assertNotNull(metricAPIService);
        JsonNode jsonNode = metricAPIService.getMetricData("applicationName", "endPoint");
        Assert.assertNull(jsonNode);
    }
}
