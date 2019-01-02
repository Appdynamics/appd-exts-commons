package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.*;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.yml.YmlReader;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

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
        ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
        AppTierNodeAPIService appTierNodeAPIService = ControllerAPIServiceFactory.getAppTierNodeAPIService();
        Assert.assertNotNull(appTierNodeAPIService);
        JsonNode jsonNode = appTierNodeAPIService.getMetricData("applicationName", "endPoint");
        Assert.assertEquals(jsonNode.get("key").asText(), "1");
    }

    @Test
    public void whenControllerClientIsNullShouldReturnNull() throws ControllerHttpRequestException{
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        ControllerClient controllerClient = null;
        ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
        AppTierNodeAPIService appTierNodeAPIService = ControllerAPIServiceFactory.getAppTierNodeAPIService();
        Assert.assertNotNull(appTierNodeAPIService);
        JsonNode jsonNode = appTierNodeAPIService.getMetricData("applicationName", "endPoint");
        Assert.assertNull(jsonNode);
    }
}
