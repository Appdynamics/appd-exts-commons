/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.dashboard.XmlControllerInfo;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.NumberUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Map;

/**
 * Created by abey.tom on 2/11/16.
 */
public class ControllerInfo {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfo.class);

    protected String controllerHost;
    protected Integer controllerPort;
    protected Boolean controllerSslEnabled;
    protected Boolean enableOrchestration;
    protected String uniqueHostId;
    protected String username;
    protected String password;
    protected String encryptedPassword;
    protected String encryptedKey;
    protected String accountAccessKey;
    protected String account;
    protected String machinePath;
    protected Boolean simEnabled;
    protected String applicationName;
    protected String tierName;
    protected String nodeName;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
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
        info.accountAccessKey = Http4ClientBuilder.getPassword(config, config);
        info.applicationName = (String) config.get("applicationName");
        info.tierName = (String) config.get("tierName");
        return info;
    }

    public static ControllerInfo fromSystemProperties() {
        ControllerInfo info = new ControllerInfo();
        info.accountAccessKey = System.getProperty("appdynamics.agent.accountAccessKey");
        info.account = System.getProperty("appdynamics.agent.accountName");
        info.applicationName = System.getProperty("appdynamics.agent.applicationName");
        info.tierName = System.getProperty("appdynamics.agent.tierName");
        info.nodeName = System.getProperty("appdynamics.agent.nodeName");
        info.controllerHost = System.getProperty("appdynamics.controller.hostName");
        info.username = System.getProperty("appdynamics.agent.monitors.controller.username");
        info.password = System.getProperty("appdynamics.agent.monitors.controller.password");
        info.encryptedKey = System.getProperty("appdynamics.agent.monitors.controller.encryptedKey");
        info.encryptedPassword = System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword");


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

        if (!Strings.isNullOrEmpty(info.accountAccessKey)) {
            this.accountAccessKey = info.accountAccessKey;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        String tmpPass;
        if (accountAccessKey != null && accountAccessKey.length() > 2) {
            tmpPass = accountAccessKey.substring(0, 2) + "******";
        } else {
            tmpPass = null;
        }
        return "CustomDashboardControllerProps{" +
                "controllerHost='" + controllerHost + '\'' +
                ", controllerPort=" + controllerPort +
                ", controllerSslEnabled=" + controllerSslEnabled +
                ", simEnabled=" + simEnabled +
                ", account='" + account + '\'' +
                ", username='" + username + '\'' +
                ", accountAccessKey='" + tmpPass + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", tierName='" + tierName + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", machinePath='" + machinePath + '\'' +
                '}';
    }

    public ControllerInfo getControllerInfo() {

        ControllerInfo controllerInfoFromSystemProps = ControllerInfo.fromSystemProperties();
        ControllerInfo controllerInfoFromXml = getControllerInfoFromXml();
        ControllerInfo controllerInfo = controllerInfoFromXml.merge(controllerInfoFromSystemProps);

        return controllerInfo;
    }

    private ControllerInfo getControllerInfoFromXml() {
        File directory = PathResolver.resolveDirectory(AManagedMonitor.class);
        logger.info("The install directory is resolved to {}", directory.getAbsolutePath());
        ControllerInfo from = null;
        if (directory.exists()) {
            File cinfo = new File(new File(directory, "conf"), "controller-info.xml");
            if (cinfo.exists()) {
                from = ControllerInfo.fromXml(cinfo);
            }
        }
        if (from == null) {
            from = new ControllerInfo();
        }
        return from;
    }

}
