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


import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by abey.tom on 2/11/16.
 */
@XmlRootElement(name = "controller-info")
public class XmlControllerInfo extends ControllerInfo {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(XmlControllerInfo.class);

    @XmlElement(name = "account-access-key")
    public String getAccountAccessKey() {
        return accountAccessKey;
    }

    @XmlElement(name = "account-name")
    public String getAccount() {
        return account;
    }

    @XmlElement(name = "controller-host")
    public String getControllerHost() {
        return controllerHost;
    }

    @XmlElement(name = "controller-port")
    public Integer getControllerPort() {
        return controllerPort;
    }

    @XmlElement(name = "controller-ssl-enabled")
    public Boolean getControllerSslEnabled() {
        return controllerSslEnabled;
    }

    @XmlElement(name = "enable-orchestration")
    public Boolean getEnableOrchestration() {
        return enableOrchestration;
    }

    @XmlElement(name = "machine-path")
    public String getMachinePath() {
        return machinePath;
    }

    @XmlElement(name = "unique-host-id")
    public String getUniqueHostId() {
        return uniqueHostId;
    }

    @XmlElement(name = "application-name")
    public String getApplicationName() {
        return applicationName;
    }

    @XmlElement(name = "sim-enabled")
    public Boolean getSimEnabled() {
        return simEnabled;
    }

    @XmlElement(name = "tier-name")
    public String getTierName() {
        return tierName;
    }

    @XmlElement(name = "node-name")
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public void setAccount(String account) {
        super.setAccount(account);
    }

    @Override
    public void setApplicationName(String applicationName) {
        super.setApplicationName(applicationName);
    }

    @Override
    public void setControllerHost(String controllerHost) {
        super.setControllerHost(controllerHost);
    }

    @Override
    public void setControllerPort(Integer controllerPort) {
        super.setControllerPort(controllerPort);
    }

    @Override
    public void setControllerSslEnabled(Boolean controllerSslEnabled) {
        super.setControllerSslEnabled(controllerSslEnabled);
    }

    @Override
    public void setEnableOrchestration(Boolean enableOrchestration) {
        super.setEnableOrchestration(enableOrchestration);
    }

    @Override
    public void setMachinePath(String machinePath) {
        super.setMachinePath(machinePath);
    }

    @Override
    public void setAccountAccessKey(String accountAccessKey) {
        super.setAccountAccessKey(accountAccessKey);
    }

    @Override
    public void setSimEnabled(Boolean simEnabled) {
        super.setSimEnabled(simEnabled);
    }

    @Override
    public void setTierName(String tierName) {
        super.setTierName(tierName);
    }

    @Override
    public void setNodeName(String nodeName) {
        super.setNodeName(nodeName);
    }

    @Override
    public void setUniqueHostId(String uniqueHostId) {
        super.setUniqueHostId(uniqueHostId);
    }
}
