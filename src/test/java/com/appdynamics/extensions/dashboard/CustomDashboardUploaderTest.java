/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by abey.tom on 10/13/15.
 */
public class CustomDashboardUploaderTest {

    @Test
    public void whenDashboardAlreadyPresentAndNotOverwriteShouldNotUpload() throws ControllerHttpRequestException {
        CustomDashboardAPIService customDashboardAPIService = mock(CustomDashboardAPIService.class);
        JsonNode rootNode = getSampleDashboardData();
        when(customDashboardAPIService.getAllDashboards()).thenReturn(rootNode);
        doNothing().when(customDashboardAPIService).uploadDashboard(isA(Map.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
        CustomDashboardUploader customDashboardUploader = new CustomDashboardUploader(customDashboardAPIService);
        String dashboardName = "ExistingDashboardName";
        String fileContents = "contents";
        Map<String, ? super Object> configMap = new HashMap<>();
        boolean overwrite = false;
        customDashboardUploader.checkAndUpload(dashboardName, fileContents, configMap, overwrite);
        verify(customDashboardAPIService, Mockito.times(0)).uploadDashboard(isA(Map.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
    }

    @Test
    public void whenDashboardAlreadyPresentAndOverwriteShouldUpload() throws ControllerHttpRequestException {
        CustomDashboardAPIService customDashboardAPIService = mock(CustomDashboardAPIService.class);
        JsonNode rootNode = getSampleDashboardData();
        when(customDashboardAPIService.getAllDashboards()).thenReturn(rootNode);
        doNothing().when(customDashboardAPIService).uploadDashboard(isA(Map.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
        CustomDashboardUploader customDashboardUploader = new CustomDashboardUploader(customDashboardAPIService);
        String dashboardName = "ExistingDashboardName";
        String fileContents = "contents";
        Map<String, ? super Object> configMap = new HashMap<>();
        boolean overwrite = true;
        customDashboardUploader.checkAndUpload(dashboardName, fileContents, configMap, overwrite);
        verify(customDashboardAPIService, Mockito.times(1)).uploadDashboard(isA(Map.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
    }

    @Test
    public void whenDashboardNotPresentShouldUpload() throws ControllerHttpRequestException {
        CustomDashboardAPIService customDashboardAPIService = mock(CustomDashboardAPIService.class);
        JsonNode rootNode = getSampleDashboardData();
        when(customDashboardAPIService.getAllDashboards()).thenReturn(rootNode);
        doNothing().when(customDashboardAPIService).uploadDashboard(isA(Map.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
        CustomDashboardUploader customDashboardUploader = new CustomDashboardUploader(customDashboardAPIService);
        String dashboardName = "NonExistingDashboardName";
        String fileContents = "contents";
        Map<String, ? super Object> configMap = new HashMap<>();
        boolean overwrite = false;
        customDashboardUploader.checkAndUpload(dashboardName, fileContents, configMap, overwrite);
        verify(customDashboardAPIService, Mockito.times(1)).uploadDashboard(isA(Map.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
    }

    private JsonNode getSampleDashboardData() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        JsonNode childNode1 = mapper.createObjectNode();
        ((ObjectNode) childNode1).put("name", "ExistingDashboardName");
        ((ObjectNode) childNode1).put("name2", "val2");
        ((ObjectNode) rootNode).put("obj1", childNode1);
        return rootNode;
    }
}