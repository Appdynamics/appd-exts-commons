/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.controller;

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

public class ControllerInfoFactory {

    private static ControllerInfo controllerInfo;
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);

    /*
       This controllerInfo is currently being called in a single threaded environment
       1st priority : config.yml
       2nd priority : System Properties
       3rd priority : Controller Info xml

       This class first gets data from controller-info.xml, then checks system properties and then config.yml and overwrites any new values as it finds them

     */
    public static ControllerInfo getControllerInfo(Map config, File installDir) {


        if (controllerInfo == null) {
            controllerInfo = getControllerInfoFromXml(installDir);
            logger.debug("The resolved properties from controller-info.xml are {}", controllerInfo);
            getSystemProperties();
            logger.debug("The resolved properties after controller-info.xml and system properties are {}", controllerInfo);
            if (config != null) {
                getConfigProps(config);
                logger.debug("The resolved properties after controller-info.xml, system properties and config.yml are {}", controllerInfo);
            }
        }

        return controllerInfo;
    }


    public static void getConfigProps(Map config) {
        if (!Strings.isNullOrEmpty((String) config.get("controllerHost"))) {
            controllerInfo.setControllerHost(config.get("controllerHost").toString());
        }
        Number port = (Number) config.get("controllerPort");
        if (port != null) {
            controllerInfo.setControllerPort(port.intValue());
        }
        if ((Boolean) config.get("controllerSslEnabled") != null) {
            controllerInfo.setControllerSslEnabled((Boolean) config.get("controllerSslEnabled"));
        }
        if ((Boolean) config.get("simEnabled") != null) {
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

        if (!Strings.isNullOrEmpty((String) config.get("encryptedPassword"))) {
            controllerInfo.setEncryptedPassword(config.get("encryptedPassword").toString());
        }
        if (!Strings.isNullOrEmpty((String) config.get("encryptionKey"))) {
            controllerInfo.setEncryptionKey(config.get("encryptionKey").toString());
        }

    }

    public static void getSystemProperties() {
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.accountAccessKey"))) {
            controllerInfo.setAccountAccessKey(System.getProperty("appdynamics.agent.accountAccessKey").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.accountName"))) {
            controllerInfo.setAccount(System.getProperty("appdynamics.agent.accountName").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.applicationName"))) {
            controllerInfo.setApplicationName(System.getProperty("appdynamics.agent.applicationName").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.tierName"))) {
            controllerInfo.setTierName(System.getProperty("appdynamics.agent.tierName").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.nodeName"))) {
            controllerInfo.setNodeName(System.getProperty("appdynamics.agent.nodeName").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.controller.hostName"))) {
            controllerInfo.setControllerHost(System.getProperty("appdynamics.controller.hostName").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.monitors.controller.username"))) {
            controllerInfo.setUsername(System.getProperty("appdynamics.agent.monitors.controller.username").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.monitors.controller.password"))) {
            controllerInfo.setPassword(System.getProperty("appdynamics.agent.monitors.controller.password").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.monitors.controller.encryptionKey"))) {
            controllerInfo.setEncryptionKey(System.getProperty("appdynamics.agent.monitors.controller.encryptionKey").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword"))) {
            controllerInfo.setEncryptedPassword(System.getProperty("appdynamics.agent.monitors.controller.encryptedPassword").toString());
        }
        if (!Strings.isNullOrEmpty((String) System.getProperty("appdynamics.agent.uniqueHostId"))) {
            controllerInfo.setUniqueHostId(System.getProperty("appdynamics.agent.uniqueHostId").toString());
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

    // Getting data from controller-info.xml

    public static ControllerInfo getControllerInfoFromXml(File directory) {
        logger.info("The install directory is resolved to {}", directory.getAbsolutePath());
        ControllerInfo from = null;
        if (directory.exists()) {
            File cinfo = new File(new File(directory, "conf"), "controller-info.xml");
            if (cinfo.exists()) {
                from = fromXml(cinfo);
            }
        }
        if (from == null) {
            from = ControllerInfo.getControllerInfo();
        }
        return from;
    }

    private static ControllerInfo fromXml(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlControllerInfo.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            XmlControllerInfo xmlControllerInfo = (XmlControllerInfo) unmarshaller.unmarshal(file);

            ControllerInfo controllerInfo = mergeValuesFromXML(xmlControllerInfo);
            return controllerInfo;
        } catch (JAXBException e) {
            String msg = "Cannot unmarshall the controller-info.xml from " + file.getAbsolutePath();
            throw new RuntimeException(msg, e);
        }
    }

    private static ControllerInfo mergeValuesFromXML(final XmlControllerInfo xmlControllerInfo) {
        ControllerInfo controllerInfo = ControllerInfo.getControllerInfo();
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
        return controllerInfo;
    }
}
