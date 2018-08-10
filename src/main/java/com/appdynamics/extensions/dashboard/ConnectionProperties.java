/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;


/**
 * Created by bhuvnesh.kumar on 8/7/18.
 */
public class ConnectionProperties {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ReplaceDefaultInfo.class);

    public static Map<String, ? super Object> getArgumentMap(ControllerInfo controllerInfo, Map config) {
        Map<String, ? super Object> argsMap = new HashMap<>();

        String user = getUsername(controllerInfo, config);
        String password = getPassword(controllerInfo, config);

        logDashboardProperties(user, password, controllerInfo);

        Map<String, ? super Object> serverMap = getServerMap(user, password, controllerInfo);

        List<Map<String, ?>> serverList = new ArrayList<>();
        serverList.add(serverMap);
        argsMap.put(SERVERS, serverList);

        Map<String, ? super Object> connectionMap = getConnectionMap();
        argsMap.put(CONNECTION, connectionMap);
        return argsMap;
    }

    private static void logDashboardProperties(String user, String password, ControllerInfo controllerInfo) {
        logger.debug("dashboard Controller Info given to extension: ");
        logger.debug("dashboard Host : " + controllerInfo.getControllerHost());
        logger.debug("dashboard Port : " + controllerInfo.getControllerPort());
        logger.debug("dashboard User : " + user);
        logger.debug("dashboard Password: " + password);
        logger.debug("dashboard UseSSL: " + controllerInfo.getControllerSslEnabled());
        logger.debug("dashboard ApplicationName: {}", controllerInfo.getApplicationName());
        logger.debug("dashboard TierName: {}", controllerInfo.getTierName());
        logger.debug("dashboard NodeName: {}", controllerInfo.getNodeName());
        logger.debug("dashboard Sim Enabled: {}", controllerInfo.getSimEnabled());
        logger.debug("dashboard Machine Path: {}", controllerInfo.getMachinePath());
    }

    private static Map<String, ? super Object> getServerMap(String user, String password, ControllerInfo controllerInfo) {
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, controllerInfo.getControllerHost());
        serverMap.put(TaskInputArgs.PORT, controllerInfo.getControllerPort().toString());
        serverMap.put(TaskInputArgs.USE_SSL, false);
        serverMap.put(TaskInputArgs.USER, user);
        serverMap.put(TaskInputArgs.PASSWORD, password);
        return serverMap;
    }


    private static Map<String, ? super Object> getConnectionMap() {
        Map<String, ? super Object> connectionMap = new HashMap<>();
        String[] sslProtocols = {TLSV_12};
        connectionMap.put(TaskInputArgs.SSL_PROTOCOL, sslProtocols);
        connectionMap.put(SSL_CERT_CHECK_ENABLED, false);
        connectionMap.put(CONNECT_TIMEOUT, 10000);
        connectionMap.put(SOCKET_TIMEOUT, 15000);
        return connectionMap;
    }

    private static String getPassword(ControllerInfo controllerInfo, Map config) {

        String password = getPasswordFromStartupArguments(controllerInfo);

        if (password == "") {
            password = getPasswordFromConfig(controllerInfo, config);
        }

        return password;
    }

    private static String getPasswordFromStartupArguments(ControllerInfo controllerInfo) {
        // Password from startup script
        Map<String, String> taskArgs = new HashMap<>();
        if (controllerInfo.getPassword() != null) {
            taskArgs.put(PASSWORD, controllerInfo.getPassword().toString());
        }
        if (controllerInfo.getEncryptedPassword() != null && controllerInfo.getEncryptedKey() != null) {
            taskArgs.put(ENCRYPTED_PASSWORD, controllerInfo.getEncryptedPassword().toString());
            taskArgs.put(ENCRYPTION_KEY, controllerInfo.getEncryptedKey().toString());

        }

        return CryptoUtil.getPassword(taskArgs);

    }

    private static String getPasswordFromConfig(ControllerInfo controllerInfo, Map config) {
        if (config.get(PASSWORD) != null) {
            return config.get(PASSWORD).toString();
        } else if (config.get(ENCRYPTED_PASSWORD) != null && config.get(ENCRYPTION_KEY) != null) {
            Map<String, String> taskArgs = new HashMap<>();
            taskArgs.put(ENCRYPTED_PASSWORD, config.get(ENCRYPTED_PASSWORD).toString());
            taskArgs.put(ENCRYPTION_KEY, config.get(ENCRYPTION_KEY).toString());

            return CryptoUtil.getPassword(taskArgs);
        }

        // singularity user key
        return controllerInfo.getAccountAccessKey().toString();

    }

    private static String getUsername(ControllerInfo controllerInfo, Map config) {
        // username from startup script
        if (controllerInfo.getUsername() != null && controllerInfo.getAccount() != null) {
            return controllerInfo.getUsername() + AT + controllerInfo.getAccount();
        } else if (config.get(USERNAME) != null && controllerInfo.getAccount() != null) {
            // username from extension config
            return config.get(USERNAME).toString() + AT + controllerInfo.getAccount();
        }
        // singularity user
        return SINGULARITY_AGENT + AT + controllerInfo.getAccount();
    }

}
