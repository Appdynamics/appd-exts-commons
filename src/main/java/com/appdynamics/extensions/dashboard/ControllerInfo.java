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

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.util.NumberUtils;
import com.google.common.base.Strings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Map;

/**
 * Created by abey.tom on 2/11/16.
 */
public class ControllerInfo {

    protected String controllerHost;
    protected Integer controllerPort;
    protected Boolean controllerSslEnabled;
    protected Boolean enableOrchestration;
    protected String uniqueHostId;
    protected String username;
    protected String password;
    protected String account;
    protected String machinePath;
    protected Boolean simEnabled;
    protected String applicationName;
    protected String tierName;
    protected String nodeName;

    public void setPassword(String password) {
        this.password = password;
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

    public static ControllerInfo fromXml(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlControllerInfo.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (XmlControllerInfo) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            String msg = "Cannot unmarshall the controller-info.xml from " + file.getAbsolutePath();
            throw new RuntimeException(msg, e);
        }
    }

    public static ControllerInfo fromYml(Map config) {
        ControllerInfo info = new ControllerInfo();
        info.controllerHost = (String) config.get("controllerHost");
        Number port = (Number) config.get("controllerPort");
        if (port != null) {
            info.controllerPort = port.intValue();
        }
        info.controllerSslEnabled = (Boolean) config.get("controllerSslEnabled");
        info.uniqueHostId = (String) config.get("uniqueHostId");
        info.account = (String) config.get("account");
        info.username = (String) config.get(TaskInputArgs.USER);
        info.password = Http4ClientBuilder.getPassword(config, config);
        info.applicationName = (String) config.get("applicationName");
        info.tierName = (String) config.get("tierName");
        return info;
    }

    public static ControllerInfo fromSystemProperties() {
        ControllerInfo info = new ControllerInfo();
        info.password = System.getProperty("appdynamics.agent.accountAccessKey");
        info.account = System.getProperty("appdynamics.agent.accountName");
        info.applicationName = System.getProperty("appdynamics.agent.applicationName");
        info.tierName = System.getProperty("appdynamics.agent.tierName");
        info.nodeName = System.getProperty("appdynamics.agent.nodeName");
        info.controllerHost = System.getProperty("appdynamics.controller.hostName");
        String port = System.getProperty("appdynamics.controller.port");
        if (NumberUtils.isNumber(port)) {
            info.controllerPort = Integer.parseInt(port);
        }
        String sslEnabled = System.getProperty("appdynamics.controller.ssl.enabled");
        if (!Strings.isNullOrEmpty(sslEnabled)) {
            info.controllerSslEnabled = Boolean.valueOf(sslEnabled.trim());
        }
        info.uniqueHostId = System.getProperty("appdynamics.agent.uniqueHostId");

        String simEnabled = System.getProperty("appdynamics.sim.enabled");
        if (!Strings.isNullOrEmpty(simEnabled)) {
            info.simEnabled = Boolean.valueOf(simEnabled.trim());
        }

        return info;
    }


    public ControllerInfo merge(ControllerInfo info) {
        if (!Strings.isNullOrEmpty(info.controllerHost)) {
            this.controllerHost = info.controllerHost;
        }
        if (info.controllerPort != null) {
            this.controllerPort = info.controllerPort;
        }
        if (info.controllerSslEnabled != null) {
            this.controllerSslEnabled = info.controllerSslEnabled;
        }
        if (info.enableOrchestration != null) {
            this.enableOrchestration = info.enableOrchestration;
        }
        if (!Strings.isNullOrEmpty(info.uniqueHostId)) {
            this.uniqueHostId = info.uniqueHostId;
        }

        if (!Strings.isNullOrEmpty(info.username)) {
            this.username = info.username;
        }
        if (!Strings.isNullOrEmpty(info.password)) {
            this.password = info.password;
        }
        if (!Strings.isNullOrEmpty(info.account)) {
            this.account = info.account;
        }


        if (info.simEnabled != null) {
            this.simEnabled = info.simEnabled;
        }
        if (!Strings.isNullOrEmpty(info.machinePath)) {
            this.machinePath = info.machinePath;
        }

        if (!Strings.isNullOrEmpty(info.applicationName)) {
            this.applicationName = info.applicationName;
        }
        if (!Strings.isNullOrEmpty(info.tierName)) {
            this.tierName = info.tierName;
        }
        if (!Strings.isNullOrEmpty(info.nodeName)) {
            this.nodeName = info.nodeName;
        }
        return this;
    }

    public String getPassword() {
        return password;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNodeName() {
        return nodeName;
    }

    @Override
    public String toString() {
        String tmpPass;
        if (password != null && password.length() > 2) {
            tmpPass = password.substring(0, 2) + "******";
        } else {
            tmpPass = null;
        }
        return "CustomDashboardControllerProps{" +
                "controllerHost='" + controllerHost + '\'' +
                ", controllerPort=" + controllerPort +
                ", controllerSslEnabled=" + controllerSslEnabled +
                ", account='" + account + '\'' +
                ", username='" + username + '\'' +
                ", password='" + tmpPass + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", tierName='" + tierName + '\'' +
                ", nodeName='" + nodeName + '\'' +
                '}';
    }
}
