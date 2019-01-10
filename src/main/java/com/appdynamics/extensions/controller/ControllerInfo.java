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

import com.appdynamics.extensions.util.CryptoUtil;

import java.util.HashMap;
import java.util.Map;

import static com.appdynamics.extensions.Constants.*;

/**
 * Created by abey.tom on 2/11/16.
 */
public class ControllerInfo {

    private String controllerHost;
    private Integer controllerPort;
    private Boolean controllerSslEnabled;
    private Boolean enableOrchestration;
    private String uniqueHostId;
    private String username;
    private String password;
    private String encryptedPassword;
    private String encryptionKey;
    private String accountAccessKey;
    private String account;
    private String machinePath;
    private Boolean simEnabled;
    private String applicationName;
    private String tierName;
    private String nodeName;

    ControllerInfo() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setAccountAccessKey(String accountAccessKey) {
        this.accountAccessKey = accountAccessKey;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setControllerHost(String controllerHost) {
        this.controllerHost = controllerHost;
    }

    public void setControllerPort(Integer controllerPort) {
        this.controllerPort = controllerPort;
    }

    public void setControllerSslEnabled(Boolean controllerSslEnabled) {
        this.controllerSslEnabled = controllerSslEnabled;
    }

    public void setEnableOrchestration(Boolean enableOrchestration) {
        this.enableOrchestration = enableOrchestration;
    }

    public void setMachinePath(String machinePath) {
        this.machinePath = machinePath;
    }

    public void setUniqueHostId(String uniqueHostId) {
        this.uniqueHostId = uniqueHostId;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setSimEnabled(Boolean simEnabled) {
        this.simEnabled = simEnabled;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAccountAccessKey() {
        return accountAccessKey;
    }

    public String getAccount() {
        return account;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getControllerHost() {
        return controllerHost;
    }

    public Integer getControllerPort() {
        return controllerPort;
    }

    public Boolean getControllerSslEnabled() {
        return controllerSslEnabled;
    }

    public Boolean getEnableOrchestration() {
        return enableOrchestration;
    }

    public String getMachinePath() {
        return machinePath;
    }

    public Boolean getSimEnabled() {
        return simEnabled;
    }

    public String getTierName() {
        return tierName;
    }

    public String getUniqueHostId() {
        return uniqueHostId;
    }

    public String getUsername() {
        return username;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        String tmpPass;
        if (password != null && password.length() > 2) {
            tmpPass = password.substring(0, 2) + "******";
        } else {
            tmpPass = null;
        }
        return "ControllerProps{" +
                "controllerHost='" + controllerHost + '\'' +
                ", controllerPort=" + controllerPort +
                ", controllerSslEnabled=" + controllerSslEnabled +
                ", simEnabled=" + simEnabled +
                ", account='" + account + '\'' +
                ", username='" + username + '\'' +
                ", password='" + tmpPass + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", tierName='" + tierName + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", machinePath='" + machinePath + '\'' +
                '}';
    }

}
