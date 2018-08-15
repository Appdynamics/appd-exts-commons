package com.appdynamics.extensions.conf;

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
        if (cInfo.getAccount() == null) {
            cInfo.setAccount("customer1");
        }
        check(TaskInputArgs.USER, cInfo.getUsername());
        check(TaskInputArgs.PASSWORD, cInfo.getPassword());
        check("account", cInfo.getAccount());
        check("applicationName", cInfo.getApplicationName());
        check("controllerHost", cInfo.getControllerHost());
        check("controllerPort", cInfo.getControllerPort());
        check("controllerSslEnabled", cInfo.getControllerSslEnabled());
        check("tierName", cInfo.getTierName());
        check("nodeName", cInfo.getNodeName());
        if (unresolvedProps != null) {
            logger.error("The following properties {} failed to resolve. Please add them to the 'customDashboard' section in config.yml", unresolvedProps);
            return false;
        }
        return true;
    }

    public void check(String propName, Object propVal) {
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
