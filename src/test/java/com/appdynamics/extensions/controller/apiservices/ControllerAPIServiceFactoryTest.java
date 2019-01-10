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

    @Test(expected = RuntimeException.class)
    public void whenControllerClientIsNullShouldThrowAnException() throws ControllerHttpRequestException{
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        ControllerClient controllerClient = null;
        ControllerAPIService controllerAPIService = ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
    }
}
