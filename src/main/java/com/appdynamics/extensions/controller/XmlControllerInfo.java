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


import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by abey.tom on 2/11/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "controller-info")
public class XmlControllerInfo {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(XmlControllerInfo.class);

    @XmlElement(name = "account-access-key")
    private String accountAccessKey;

    @XmlElement(name = "account-name")
    private String account;

    @XmlElement(name = "controller-host")
    private String controllerHost;

    @XmlElement(name = "controller-port")
    private Integer controllerPort;

    @XmlElement(name = "controller-ssl-enabled")
    private Boolean controllerSslEnabled;

    @XmlElement(name = "enable-orchestration")
    private Boolean enableOrchestration;

    @XmlElement(name = "machine-path")
    private String machinePath;

    @XmlElement(name = "unique-host-id")
    private String uniqueHostId;

    @XmlElement(name = "application-name")
    private String applicationName;

    @XmlElement(name = "sim-enabled")
    private Boolean simEnabled;

    @XmlElement(name = "tier-name")
    private String tierName;

    @XmlElement(name = "node-name")
    private String nodeName;


    public String getAccountAccessKey() {
        return accountAccessKey;
    }

    public String getAccount() {
        return account;
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

    public String getUniqueHostId() {
        return uniqueHostId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Boolean getSimEnabled() {
        return simEnabled;
    }

    public String getTierName() {
        return tierName;
    }

    public String getNodeName() {
        return nodeName;
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

}
