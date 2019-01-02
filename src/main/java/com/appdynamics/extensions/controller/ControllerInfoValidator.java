/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.controller;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.appdynamics.extensions.Constants.PASSWORD;
import static com.appdynamics.extensions.Constants.USER;

public class ControllerInfoValidator {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);
    private List<String> unresolvedProps;
    private ControllerInfo controllerInfo;

    public ControllerInfoValidator(ControllerInfo controllerInfo) {
        this.controllerInfo = controllerInfo;
    }

    public boolean isValidatedAndResolved() {
        if (controllerInfo == null) {
            return false;
        }
        // #TODO Need to check why this is being set.
        if (controllerInfo.getAccount() == null) {
            controllerInfo.setAccount("customer1");
        }
        check(USER, controllerInfo.getUsername());
        check(PASSWORD, controllerInfo.getPassword());
        check("account", controllerInfo.getAccount());
        check("controllerHost", controllerInfo.getControllerHost());
        check("controllerPort", controllerInfo.getControllerPort());
        check("controllerSslEnabled", controllerInfo.getControllerSslEnabled());
        /*Incase the the machine agent is running in standalone mode and there is atleast one app agent, then there is
        need to check for app, tier and node. Right now we are not supporting this case as per below condition.*/
        if (!isSimEnabled(controllerInfo)) {
            checkAppTierNode(controllerInfo);
        }
        if (unresolvedProps != null) {
            logger.error("The following properties {} failed to resolve. Please add them to the 'customDashboard' " +
                    "section in config.yml", unresolvedProps);
            return false;
        }
        return true;
    }

    private void checkAppTierNode(ControllerInfo controllerInfo) {
        check("applicationName", controllerInfo.getApplicationName());
        check("tierName", controllerInfo.getTierName());
        check("nodeName", controllerInfo.getNodeName());
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

    private boolean isSimEnabled(ControllerInfo controllerInfo) {
        Object propVal = controllerInfo.getSimEnabled();
        if (propVal != null) {
            if (!(propVal instanceof Boolean)) {
                markUnresolved("simEnabled");
                return false;
            } else {
                return (Boolean) propVal;
            }
        } else {
            markUnresolved("simEnabled");
            return false;
        }
    }

    private void markUnresolved(String propName) {
        if (unresolvedProps == null) {
            unresolvedProps = new ArrayList<String>();
        }
        unresolvedProps.add(propName);
    }
}
