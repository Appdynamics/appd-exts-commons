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

package com.appdynamics.extensions.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 1/3/19.
 */
public class ControllerClientTest {
    @Test
    public void whenHTTPCallSuccessfulShouldReturnValidData() throws ControllerHttpRequestException, Exception {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(httpClient.execute(isA(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        InputStream inputStream = new FileInputStream("src/test/resources/controller/sampleresponse.txt");
        httpEntity.setContent(inputStream);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(200);
        ControllerClient controllerClient = new ControllerClient();
        controllerClient.setHttpClient(httpClient);
        controllerClient.setBaseURL("testBaseURL");
        CookiesCsrf cookiesCsrf = mock(CookiesCsrf.class);
        when(cookiesCsrf.getCsrf()).thenReturn("testCsrf");
        controllerClient.setCookiesCsrf(cookiesCsrf);
        String response = controllerClient.sendGetRequest("testURL");
        Assert.assertNotNull(response);
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        Assert.assertEquals(jsonNode.get("key1").asText(), "1");
        Assert.assertEquals(jsonNode.get("key2").asText(), "2");
        inputStream.close();
    }

    @Test
    public void whenHTTPCallReturnsFailedStatusShouldReturnNullData() throws ControllerHttpRequestException, Exception {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(httpClient.execute(isA(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(404);
        ControllerClient controllerClient = new ControllerClient();
        controllerClient.setHttpClient(httpClient);
        controllerClient.setBaseURL("testBaseURL");
        CookiesCsrf cookiesCsrf = mock(CookiesCsrf.class);
        when(cookiesCsrf.getCsrf()).thenReturn("testCsrf");
        controllerClient.setCookiesCsrf(cookiesCsrf);
        String response = controllerClient.sendGetRequest("testURL");
        Assert.assertNull(response);
    }

    @Test(expected = ControllerHttpRequestException.class)
    public void whenHTTPCallFailsShouldThrowAnException() throws ControllerHttpRequestException, Exception {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(404);
        ControllerClient controllerClient = new ControllerClient();
        controllerClient.setHttpClient(httpClient);
        controllerClient.setBaseURL("testBaseURL");
        CookiesCsrf cookiesCsrf = mock(CookiesCsrf.class);
        when(cookiesCsrf.getCsrf()).thenReturn("testCsrf");
        controllerClient.setCookiesCsrf(cookiesCsrf);
        String response = controllerClient.sendGetRequest("testURL");
    }
}
