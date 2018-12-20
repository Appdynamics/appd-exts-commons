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

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.CookiesCsrf;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Created by abey.tom on 10/13/15.
 */
public class CustomDashboardUploaderTest {

    @Test
    public void testUploaderWhenDashboardAlreadyPresent() throws ControllerHttpRequestException {
        ControllerInfo controllerInfo = mock(ControllerInfo.class);
        ControllerClient controllerClient = mock(ControllerClient.class);
        //CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CookiesCsrf cookiesCsrf = mock(CookiesCsrf.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        JsonNode childNode1 = mapper.createObjectNode();
        ((ObjectNode) childNode1).put("name", "DashboardName");
        ((ObjectNode) childNode1).put("name2", "val2");
        ((ObjectNode) rootNode).put("obj1", childNode1);
        Mockito.when(controllerClient.getCookiesCsrf()).thenReturn(cookiesCsrf);
        Mockito.when(controllerClient.sendGetRequest(anyString())).thenReturn(rootNode.toString());
        //Mockito.doNothing().when(customDashboardUploader).uploadDashboard(isA(Map.class), isA(CookiesCsrf.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
        CustomDashboardUploader customDashboardUploader = new CustomDashboardUploader(controllerInfo, controllerClient);
        String dashboardName = "DashboardName";
        String fileContents = "contents";
        Map<String, ? super Object> argsMap = new HashMap<>();
        boolean overwrite = false;
        customDashboardUploader.checkAndUpload(dashboardName, fileContents, argsMap, overwrite);
        /*verify(customDashboardUploader).uploadDashboard();
        Mockito.verify(apiService, Mockito.times(0)).uploadDashboard(new HashMap(), cookiesCsrf, emptyStringForTest,
                emptyStringForTest, emptyStringForTest, emptyStringForTest);*/

    }

    @Test
    public void testUploaderWhenDashboardNotPresent() throws ControllerHttpRequestException {
        /*CloseableHttpClient client = mock(CloseableHttpClient.class);
        CookiesCsrf cookiesCsrf = mock(CookiesCsrf.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        JsonNode childNode1 = mapper.createObjectNode();
        ((ObjectNode) childNode1).put("name", "DashboardName2");
        ((ObjectNode) childNode1).put("name2", "val2");
        ((ObjectNode) rootNode).put("obj1", childNode1);
        Mockito.when(apiService.getCookiesAndAuthToken(client)).thenReturn(cookiesCsrf);
        Mockito.when(apiService.getAllDashboards(client, cookiesCsrf)).thenReturn(rootNode);
        Mockito.doNothing().when(apiService).uploadDashboard(isA(Map.class), isA(CookiesCsrf.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));

        CustomDashboardUploader customDashboardUploader = new CustomDashboardUploader(apiService);
        String dashboardName = "DashboardName";
        String fileContents = "contents";
        Map<String, ? super Object> argsMap = new HashMap<>();
        boolean overwrite = false;
        customDashboardUploader.checkAndUpload( client, dashboardName, fileContents, argsMap, overwrite);
        Mockito.verify(apiService, Mockito.times(1)).uploadDashboard(new HashMap(), cookiesCsrf, dashboardName,
                "json", fileContents, "application/json");*/

    }
}