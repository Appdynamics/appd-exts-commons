package com.appdynamics.extensions.controller;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.TaskInputArgs.*;

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerClientBuilder {

    private ControllerInfo controllerInfo;
    private Map<String, ?> connectionMap;
    private Map<String, ?> proxyMap;
    private HttpClientBuilder httpClientBuilder;
    private String controllerBaseURL;

    ControllerClientBuilder(ControllerInfo controllerInfo, Map<String, ?> connectionMap, Map<String, ?> proxyMap) {
        this.controllerInfo = controllerInfo;
        this.connectionMap = connectionMap;
        this.proxyMap = proxyMap;
        controllerBaseURL = buildURI(controllerInfo.getControllerHost(), String.valueOf(controllerInfo.getControllerPort()), controllerInfo.getControllerSslEnabled());
        httpClientBuilder = Http4ClientBuilder.getBuilder(getPropMap());
    }

    private String buildURI(String host, String port, boolean sslEnabled) {
        StringBuilder sb = new StringBuilder();
        if (sslEnabled) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(host).append(":").append(port).append("/");
        return sb.toString();
    }

    private Map<String, ?> getPropMap() {
        Map<String, Object> propMap = Maps.newHashMap();
        propMap.put("servers", getServersList());
        if(connectionMap != null) {
            propMap.put("connection", connectionMap);
        }
        if(proxyMap != null) {
            propMap.put("proxy", proxyMap);
        }
        return propMap;
    }

    private List<Map<String, Object>> getServersList() {
        List<Map<String, Object>> serversList = Lists.newArrayList();
        Map<String, Object> controllerServerMap = Maps.newHashMap();
        controllerServerMap.put(URI, getControllerBaseURL());
        controllerServerMap.put(USERNAME, getUserName());
        controllerServerMap.put(PASSWORD, controllerInfo.getPassword());
        controllerServerMap.put(ENCRYPTED_PASSWORD, controllerInfo.getEncryptedPassword());
        serversList.add(controllerServerMap);
        return serversList;
    }

    private String getUserName() {
        String accountName = controllerInfo.getAccount();
        if (accountName != null) {
            return "singularity-agent@" + accountName;
        }
        return "";
    }

    HttpClientBuilder getControllerClientBuilder() {
        return httpClientBuilder;
    }

    String getControllerBaseURL() {
        return controllerBaseURL;
    }
 }
