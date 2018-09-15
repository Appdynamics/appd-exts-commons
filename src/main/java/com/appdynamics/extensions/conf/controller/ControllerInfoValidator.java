/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.controller;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ControllerInfoValidator {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);

    private List<String> unresolvedProps;

    public boolean validateAndCheckIfResolved(ControllerInfo cInfo) {
        logger.debug("Validator Check CINFO : {}", cInfo);
        if (cInfo.getAccount() == null) {
            cInfo.setAccount("customer1");
        }
        check(TaskInputArgs.USER, cInfo.getUsername());
        check(TaskInputArgs.PASSWORD, cInfo.getPassword());
        check("account", cInfo.getAccount());
        check("controllerHost", cInfo.getControllerHost());
        check("controllerPort", cInfo.getControllerPort());
        check("controllerSslEnabled", cInfo.getControllerSslEnabled());

        simEnabledOrNot(cInfo);
        if (unresolvedProps != null) {
            logger.error("The following properties {} failed to resolve. Please add them to the 'customDashboard' section in config.yml", unresolvedProps);
            return false;
        }
        return true;
    }

    //TODO write test case if controllerinfo.xml does not have sim enabled field at all, does it return null/empty?
    private void simEnabledOrNot(ControllerInfo cInfo) {
        Object propVal = cInfo.getSimEnabled();
        if (propVal != null) {
            if (propVal instanceof Boolean) {
                if (((Boolean) propVal).booleanValue() == false) {
                    checkAppTierNode(cInfo);
                }
            }
        } else if (cInfo.getApplicationName() != null && cInfo.getTierName() != null && cInfo.getNodeName() != null) {
            checkAppTierNode(cInfo);
        } else {
            markUnresolved("controllerSslEnabled");
        }
    }

    private void checkAppTierNode(ControllerInfo cInfo) {
        check("applicationName", cInfo.getApplicationName());
        check("tierName", cInfo.getTierName());
        check("nodeName", cInfo.getNodeName());
    }


    private void check(String propName, Object propVal) {
        if (propVal != null) {
            if (propVal instanceof String) {
                if (Strings.isNullOrEmpty((String) propVal)) {
                    markUnresolved(propName);
                }
            }
        } else {
            markUnresolved(propName);
        }

    }

    private void markUnresolved(String propName) {
        if (unresolvedProps == null) {
            unresolvedProps = new ArrayList<String>();
        }
        unresolvedProps.add(propName);
    }

}
