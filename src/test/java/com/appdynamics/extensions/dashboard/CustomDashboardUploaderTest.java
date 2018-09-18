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

import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.api.CookiesCsrf;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.isA;

/**
 * Created by abey.tom on 10/13/15.
 */
public class CustomDashboardUploaderTest {

    @Test
    public void testUploader() {
        ControllerApiService apiService = Mockito.mock(ControllerApiService.class);
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        CookiesCsrf cookiesCsrf = Mockito.mock(CookiesCsrf.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();

        JsonNode childNode1 = mapper.createObjectNode();
        ((ObjectNode) childNode1).put("name", "DashboardName");
        ((ObjectNode) childNode1).put("name2", "val2");
        ((ObjectNode) rootNode).put("obj1", childNode1);

        try {
            Mockito.when(apiService.getCookiesAndAuthToken(client)).thenReturn(cookiesCsrf);
            Mockito.when(apiService.getAllDashboards(client, cookiesCsrf)).thenReturn(rootNode);

            Map map = new HashMap();
            String stringing = "";
            Mockito.doNothing().when(apiService).uploadDashboard(isA(Map.class), isA(CookiesCsrf.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
            apiService.uploadDashboard(map, cookiesCsrf, stringing, stringing, stringing, stringing);

            CustomDashboardUploader customDashboardUploader = new CustomDashboardUploader();
            String dashboardName = "DashboardName";
            String fileContents = "contents";
            Map<String, ? super Object> argsMap = new HashMap<>();
            boolean overwrite = false;
            customDashboardUploader.gatherDashboardDataToUpload(apiService, client,dashboardName, fileContents,  argsMap, overwrite );
            Mockito.verify(apiService, Mockito.times(1)).uploadDashboard(map, cookiesCsrf, stringing, stringing, stringing, stringing);

        } catch (ApiException e) {
            Assert.assertFalse(true);
        }

    }

    @Test
    public void testUploadDashboard() throws Exception {
        /*String content = FileUtils.readFileToString(new File("src/test/resources/dashboard/test-custom-dashboard-template.xml"));

        Map<String,? super Object> argsMap = new HashMap<>();

        List<Map<String, ?>> serverList = new ArrayList<>();
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, "");
        serverMap.put(TaskInputArgs.PORT, "");
        serverMap.put(TaskInputArgs.USE_SSL, false);
        serverMap.put(TaskInputArgs.USER, "");
        serverMap.put(TaskInputArgs.PASSWORD, "");
        serverList.add(serverMap);
        argsMap.put("servers", serverList);

        Map<String, ? super Object> connectionMap = new HashMap<>();
        String[] sslProtocols = {"TLSv1.2"};
        connectionMap.put(TaskInputArgs.SSL_PROTOCOL, sslProtocols);
        connectionMap.put("sslCertCheckEnabled", false);
        connectionMap.put("connectTimeout", 10000);
        connectionMap.put("socketTimeout", 15000);
        argsMap.put("connection", connectionMap);


        new CustomDashboardUploader().gatherDashboardDataToUpload("Test1", Xml.fromString(content),argsMap,false);*/
    }
}