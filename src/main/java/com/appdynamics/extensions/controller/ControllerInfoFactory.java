/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.controller;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.NumberUtils;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Map;

/**
 * This controllerInfo is currently being called in a single threaded environment
 * 1st priority : config.yml
 * 2nd priority : System Properties
 * 3rd priority : Controller Info xml
 * This class first gets data from controller-info.xml, then checks system properties and then config.yml and overwrites any new values as it finds them
 */

public class ControllerInfoFactory {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);
    private static final ControllerInfo controllerInfo = new ControllerInfo();

    public static ControllerInfo initialize(Map config, File installDir) {
        // #TODO There should be a clear method for all the previously set fields. Uncomment the unit test in ControllerModuleTest accordingly. Also check ControllerInfo test.
        if(installDir != null) {
            getControllerInfoFromXml(installDir);
        }
        logger.debug("The resolved properties from controller-info.xml are {}", controllerInfo);
        getControllerInfoFromSystemProperties();
        logger.debug("The resolved properties after controller-info.xml and system properties are {}", controllerInfo);
        if (config != null) {
            getControllerInfoFromYml(config);
            logger.debug("The resolved properties after controller-info.xml, system properties and config.yml are {}", controllerInfo);
        }
        /*
        #TODO FULL AGENT RESOLVER
        1. MA with one app agent, automatically resolve app, tier and node.
        2. *MA with multiple app agents does *require* resolving. It can have non sim and non AppTierNode, for this case have a separate field in the controllerinfo*
        3. MA with no agents, throw a run time exception.
        */
        return controllerInfo;
    }

    private static void getControllerInfoFromXml(File directory) {
        logger.info("The install directory is resolved to {}", directory.getAbsolutePath());
        if (directory.exists()) {
            File xmlControllerInfoFile = new File(new File(directory, "conf"), "controller-info.xml");
            if (xmlControllerInfoFile.exists()) {
                fromXml(xmlControllerInfoFile);
            }
        }
    }

    //#TODO Check the flag appdynamics.machine.agent.configFile & remove the exception instead log a warn statement
    private static void fromXml(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlControllerInfo.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            XmlControllerInfo xmlControllerInfo = (XmlControllerInfo) unmarshaller.unmarshal(file);
            mergeValuesFromXML(xmlControllerInfo);
        } catch (JAXBException e) {
            String msg = "Cannot unmarshall the controller-info.xml from " + file.getAbsolutePath();
            throw new RuntimeException(msg, e);
        }
    }

    private static void mergeValuesFromXML(final XmlControllerInfo xmlControllerInfo) {
        controllerInfo.setAccountAccessKey(xmlControllerInfo.getAccountAccessKey());
        controllerInfo.setAccount(xmlControllerInfo.getAccount());
        controllerInfo.setControllerHost(xmlControllerInfo.getControllerHost());
        controllerInfo.setControllerPort(xmlControllerInfo.getControllerPort());
        controllerInfo.setControllerSslEnabled(xmlControllerInfo.getControllerSslEnabled());
        controllerInfo.setEnableOrchestration(xmlControllerInfo.getEnableOrchestration());
        controllerInfo.setMachinePath(xmlControllerInfo.getMachinePath());
        controllerInfo.setUniqueHostId(xmlControllerInfo.getUniqueHostId());
        controllerInfo.setNodeName(xmlControllerInfo.getNodeName());
        controllerInfo.setTierName(xmlControllerInfo.getTierName());
        controllerInfo.setApplicationName(xmlControllerInfo.getApplicationName());
        controllerInfo.setSimEnabled(xmlControllerInfo.getSimEnabled());
    }

    private static void getControllerInfoFromSystemProperties() {
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.accountAccessKey"))) {
            controllerInfo.setAccountAccessKey(System.getProperty("appdynamics.agent.accountAccessKey"));
        }
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.accountName"))) {
            controllerInfo.setAccount(System.getProperty("appdynamics.agent.accountName"));
        }
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.applicationName"))) {
            controllerInfo.setApplicationName(System.getProperty("appdynamics.agent.applicationName"));
        }
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.tierName"))) {
            controllerInfo.setTierName(System.getProperty("appdynamics.agent.tierName"));
        }
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.nodeName"))) {
            controllerInfo.setNodeName(System.getProperty("appdynamics.agent.nodeName"));
        }
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.controller.hostName"))) {
            controllerInfo.setControllerHost(System.getProperty("appdynamics.controller.hostName"));
        }
        //#TODO The naming has to be according to our new conventions, all these kind of constants need to be in a separate constants file
        // called MonitorConstants at the ABaseMonitor level.
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.monitors.controller.username"))) {
            controllerInfo.setUsername(System.getProperty("appdynamics.agent.monitors.controller.username"));
        }
        //#TODO The naming has to be according to our new conventions, all these kind of constants need to be in a separate constants file
        // called MonitorConstants at the ABaseMonitor level.
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.monitors.controller.password"))) {
            controllerInfo.setPassword(System.getProperty("appdynamics.agent.monitors.controller.password"));
        }
        //#TODO The naming has to be according to our new conventions, all these kind of constants need to be in a separate constants file
        // called MonitorConstants at the ABaseMonitor level.
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.monitors.controller.encryptionKey"))) {
            controllerInfo.setEncryptionKey(System.getProperty("appdynamics.agent.monitors.controller.encryptionKey"));
        }
        //#TODO The naming has to be according to our new conventions, all these kind of constants need to be in a separate constants file
        // called MonitorConstants at the ABaseMonitor level.
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword"))) {
            controllerInfo.setEncryptedPassword(System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword"));
        }
        if (!Strings.isNullOrEmpty(System.getProperty("appdynamics.agent.uniqueHostId"))) {
            controllerInfo.setUniqueHostId(System.getProperty("appdynamics.agent.uniqueHostId"));
        }
        String port = System.getProperty("appdynamics.controller.port");
        if (NumberUtils.isNumber(port)) {
            controllerInfo.setControllerPort(Integer.parseInt(port));
        }
        String sslEnabled = System.getProperty("appdynamics.controller.ssl.enabled");
        if (!Strings.isNullOrEmpty(sslEnabled)) {
            controllerInfo.setControllerSslEnabled(Boolean.valueOf(sslEnabled.trim()));
        }
        String simEnabled = System.getProperty("appdynamics.sim.enabled");
        if (!Strings.isNullOrEmpty(simEnabled)) {
            controllerInfo.setSimEnabled(Boolean.valueOf(simEnabled.trim()));
        }
    }

    private static void getControllerInfoFromYml(Map config) {
        if (!Strings.isNullOrEmpty((String) config.get("controllerHost"))) {
            controllerInfo.setControllerHost(config.get("controllerHost").toString());
        }
        Number port = (Number) config.get("controllerPort");
        if (port != null) {
            controllerInfo.setControllerPort(port.intValue());
        }
        if (config.get("controllerSslEnabled") != null) {
            controllerInfo.setControllerSslEnabled((Boolean) config.get("controllerSslEnabled"));
        }
        if (config.get("simEnabled") != null) {
            controllerInfo.setSimEnabled((Boolean) config.get("simEnabled"));
        }
        if (!Strings.isNullOrEmpty((String) config.get("uniqueHostId"))) {
            controllerInfo.setUniqueHostId(config.get("uniqueHostId").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("account"))) {
            controllerInfo.setAccount(config.get("account").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("username"))) {
            controllerInfo.setUsername(config.get("username").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("password"))) {
            controllerInfo.setPassword(Http4ClientBuilder.getPassword(config, config));
        }
        if (!Strings.isNullOrEmpty((String) config.get("accountAccessKey"))) {
            controllerInfo.setAccountAccessKey(config.get("accountAccessKey").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("applicationName"))) {
            controllerInfo.setApplicationName(config.get("applicationName").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("tierName"))) {
            controllerInfo.setTierName(config.get("tierName").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("nodeName"))) {
            controllerInfo.setNodeName(config.get("nodeName").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("machinePath"))) {
            controllerInfo.setMachinePath(config.get("machinePath").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("encryptedPassword"))) {
            controllerInfo.setEncryptedPassword(config.get("encryptedPassword").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("encryptionKey"))) {
            controllerInfo.setEncryptionKey(config.get("encryptionKey").toString());
        }
    }
}
