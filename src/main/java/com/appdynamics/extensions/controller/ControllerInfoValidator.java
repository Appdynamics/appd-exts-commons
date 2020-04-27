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

    public boolean isValidated() {
        if (controllerInfo == null) {
            return false;
        }
        check(USER, controllerInfo.getUsername());
        check(PASSWORD, controllerInfo.getPassword());
        check("account", controllerInfo.getAccount());
        check("controllerHost", controllerInfo.getControllerHost());
        check("controllerPort", controllerInfo.getControllerPort());
        check("controllerSslEnabled", controllerInfo.getControllerSslEnabled());
        if (!isSimEnabled(controllerInfo)) {
            checkAppTierNode(controllerInfo);
        }
        if (unresolvedProps != null) {
            logger.error("The following properties {} failed to resolve. Please add them to the 'controllerInfo' " +
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
