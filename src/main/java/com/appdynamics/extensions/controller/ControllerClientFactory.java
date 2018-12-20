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

    private ControllerInfo controllerInfo;
    private Map<String, ?> connectionMap;
    private Map<String, ?> proxyMap;
    private String encryptionKey;
    private String controllerBaseURL;
    private HttpClientBuilder httpClientBuilder;
    private ControllerClient controllerClient;

    public ControllerClientFactory(ControllerInfo controllerInfo, Map<String, ?> connectionMap, Map<String, ?> proxyMap, String encryptionKey) {
        this.controllerInfo = controllerInfo;
        this.connectionMap = connectionMap;
        this.proxyMap = proxyMap;
        this.encryptionKey = encryptionKey;
        controllerBaseURL = buildURI(controllerInfo.getControllerHost(), String.valueOf(controllerInfo.getControllerPort()), controllerInfo.getControllerSslEnabled());
        httpClientBuilder = Http4ClientBuilder.getBuilder(getPropMap());
        initialize();
    }

    private void initialize() {
        controllerClient = ControllerClient.getControllerClient();
        controllerClient.setControllerHttpClient(httpClientBuilder.build());
        controllerClient.setControllerBaseURL(controllerBaseURL);
    }

    public ControllerClient getControllerClient() {
        return controllerClient;
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
        if(!Strings.isNullOrEmpty(encryptionKey)) {
            propMap.put(ENCRYPTION_KEY, encryptionKey);
        }
        return propMap;
    }

    private List<Map<String, Object>> getServersList() {
        List<Map<String, Object>> serversList = Lists.newArrayList();
        Map<String, Object> controllerServerMap = Maps.newHashMap();
        controllerServerMap.put(URI, controllerBaseURL);
        controllerServerMap.put(USER, getUserName());
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
    private String getUserName() {
        String username = controllerInfo.getUsername();
        String accountName = controllerInfo.getAccount();
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(accountName)) {
            return username + "@" + accountName;
        }
        return "";
    }
 }
