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

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.Constants.*;

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerClientFactory {
    private static String controllerBaseURL;
    private static HttpClientBuilder httpClientBuilder;
    private static final ControllerClient controllerClient = new ControllerClient();

    public static ControllerClient initialize(ControllerInfo controllerInfo, Map<String, ?> connectionMap, Map<String, ?> proxyMap, String encryptionKey) {
        resetControllerClient();
        controllerBaseURL = buildURI(controllerInfo.getControllerHost(), String.valueOf(controllerInfo.getControllerPort()), controllerInfo.getControllerSslEnabled());
        httpClientBuilder = Http4ClientBuilder.getBuilder(getPropMap(controllerInfo, connectionMap, proxyMap, encryptionKey));
        initializeControllerClient();
        return controllerClient;
    }

    private static void resetControllerClient() {
        controllerClient.setBaseURL(null);
        controllerClient.setHttpClient(null);
        controllerClient.setCookiesCsrf(null);
    }

    private static void initializeControllerClient() {
        controllerClient.setHttpClient(httpClientBuilder.build());
        controllerClient.setBaseURL(controllerBaseURL);
    }

    private static String buildURI(String host, String port, boolean sslEnabled) {
        StringBuilder sb = new StringBuilder();
        if (sslEnabled) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(host).append(":").append(port).append("/");
        return sb.toString();
    }

    private static Map<String, ?> getPropMap(ControllerInfo controllerInfo, Map<String, ?> connectionMap, Map<String, ?> proxyMap, String encryptionKey) {
        Map<String, Object> propMap = Maps.newHashMap();
        propMap.put("servers", getServersList(controllerInfo));
        if(connectionMap != null) {
            propMap.put("connection", connectionMap);
        }
        if(proxyMap != null) {
            propMap.put("proxy", proxyMap);
        }
        if(!Strings.isNullOrEmpty(encryptionKey)) {
            propMap.put(ENCRYPTION_KEY, encryptionKey);
        }
        return propMap;
    }

    private static List<Map<String, Object>> getServersList(ControllerInfo controllerInfo) {
        List<Map<String, Object>> serversList = Lists.newArrayList();
        Map<String, Object> controllerServerMap = Maps.newHashMap();
        controllerServerMap.put(URI, controllerBaseURL);
        controllerServerMap.put(USER, getUserName(controllerInfo));
        controllerServerMap.put(PASSWORD, controllerInfo.getPassword());
        controllerServerMap.put(ENCRYPTED_PASSWORD, controllerInfo.getEncryptedPassword());
        serversList.add(controllerServerMap);
        return serversList;
    }

    /*
    * The singularity user no longer works for the controller APIs, atleast in 4.4+.
    * So the username that works is username@accountName
    * eg: admin@customer1
    * */
    private static String getUserName(ControllerInfo controllerInfo) {
        String username = controllerInfo.getUsername();
        String accountName = controllerInfo.getAccount();
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(accountName)) {
            return username + "@" + accountName;
        }
        return "";
    }
 }
