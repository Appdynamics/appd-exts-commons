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

package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.yml.YmlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * @author : Aditya Jagtiani
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({EventsServiceDataManager.class, Http4ClientBuilder.class, HttpClientBuilder.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class EventsServiceDataManagerTest {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceDataManagerTest.class);
    private EventsServiceDataManager eventsServiceDataManager;
    private Map<String, ?> eventsServiceParams;
    private CloseableHttpClient httpClient;

    @Before
    public void initialize() throws Exception {
        httpClient = mock(CloseableHttpClient.class);
        eventsServiceParams = (Map) YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"))
                .get("eventsServiceParameters");
        mockStatic(HttpClientBuilder.class);
        HttpClientBuilder httpClientBuilder = mock(HttpClientBuilder.class);
        when(HttpClientBuilder.class, "create").thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpClient);
    }

    @Test
    public void createSchemaTest() throws Exception {
        HttpPost httpPost = mock(HttpPost.class);
        whenNew(HttpPost.class).withAnyArguments().thenReturn(httpPost);
        doNothing().when(httpPost).setHeader(anyString(), anyString());
        doNothing().when(httpPost).setEntity(isA(StringEntity.class));
        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams);
        eventsServiceDataManager.createSchema("schema1", FileUtils.readFileToString(new File("src/" +
                "test/resources/eventsservice/createSchema.json")));
        verify(httpClient, times(1)).execute(httpPost);
    }

    @Test
    public void retrieveSchemaTestWhenSchemaExists() throws Exception {
        HttpGet httpGet = mock(HttpGet.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        StringEntity entity = new StringEntity("SchemaBody");

        whenNew(HttpGet.class).withAnyArguments().thenReturn(httpGet);
        doNothing().when(httpGet).setHeader(anyString(), anyString());

        when(httpClient.execute(httpGet)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(entity);

        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams);
        Assert.assertEquals("SchemaBody", eventsServiceDataManager.retrieveSchema("schema1"));
    }

    @Test
    public void retrieveSchemaTestWhenSchemaDoesNotExist() throws Exception {
        HttpGet httpGet = mock(HttpGet.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        StringEntity entity = new StringEntity("SchemaBody");

        whenNew(HttpGet.class).withAnyArguments().thenReturn(httpGet);
        doNothing().when(httpGet).setHeader(anyString(), anyString());

        when(httpClient.execute(httpGet)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(400);
        when(httpResponse.getEntity()).thenReturn(entity);

        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams);
        Assert.assertEquals("", eventsServiceDataManager.retrieveSchema("schema1"));
    }

    @Test
    public void updateSchemaTest() throws Exception {
        HttpPatch httpPatch = mock(HttpPatch.class);
        whenNew(HttpPatch.class).withAnyArguments().thenReturn(httpPatch);
        doNothing().when(httpPatch).setHeader(anyString(), anyString());
        doNothing().when(httpPatch).setEntity(isA(StringEntity.class));
        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams) {
            @Override
            public String retrieveSchema(String schemaName) {
                return "schema1";
            }
        };
        eventsServiceDataManager.updateSchema("Schema1", FileUtils.readFileToString(new File("src/test/" +
                "resources/eventsservice/updateSchema.json")));
        verify(httpClient, times(1)).execute(httpPatch);
    }

    @Test
    public void deleteSchemaTest() throws Exception {
        HttpDelete httpDelete = mock(HttpDelete.class);
        whenNew(HttpDelete.class).withAnyArguments().thenReturn(httpDelete);
        doNothing().when(httpDelete).setHeader(anyString(), anyString());
        List<String> schemasToBeDeleted = Lists.newArrayList();
        schemasToBeDeleted.add("schema1");
        schemasToBeDeleted.add("schema2");
        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams) {
            @Override
            public String retrieveSchema(String schemaName) {
                return "schema1";
            }
        };
        eventsServiceDataManager.deleteSchema(schemasToBeDeleted);
        verify(httpClient, times(2)).execute(httpDelete);
    }

    @Test
    public void publishEventTest() throws Exception {
        HttpPost httpPost = mock(HttpPost.class);
        whenNew(HttpPost.class).withAnyArguments().thenReturn(httpPost);
        doNothing().when(httpPost).setHeader(anyString(), anyString());
        doNothing().when(httpPost).setEntity(isA(StringEntity.class));
        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams) {
            @Override
            public String retrieveSchema(String schemaName) {
                return "schema1";
            }
        };
        eventsServiceDataManager.publishEvents("schema1", generateEventsFromFile(new File("src/test/" +
                "resources/eventsservice/publishEvents.json")));
        verify(httpClient, times(2)).execute(httpPost);
    }

    private List<String> generateEventsFromFile(File eventsFromFile) {
        List<String> eventsToBePublishedForSchema = Lists.newArrayList();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(eventsFromFile);
            for (JsonNode node : arrayNode) {
                eventsToBePublishedForSchema.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
            }
        } catch (Exception ex) {
            LOGGER.error("Error encountered while generating events from file: {}", eventsFromFile.getAbsolutePath(), ex);
        }
        return eventsToBePublishedForSchema;
    }
}