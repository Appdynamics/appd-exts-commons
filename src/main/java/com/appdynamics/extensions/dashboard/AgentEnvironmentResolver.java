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
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 4/10/15.
 */
public class AgentEnvironmentResolver {
    public static final org.slf4j.Logger logger = ExtensionsLoggerFactory.getLogger(AgentEnvironmentResolver.class);
    private ControllerInfo cInfo;
    private boolean resolved;
    private Map dashboardConfig;
    private List<String> unresolvedProps;

    public AgentEnvironmentResolver(Map dashboardConfig) {
        ControllerInfo cInfoYml = ControllerInfo.fromYml(dashboardConfig);
        logger.debug("The resolved properties from yml are {}", cInfoYml);
        ControllerInfo cInfoSysProp = ControllerInfo.fromSystemProperties();
        logger.debug("The resolved properties from SysProps are {}", cInfoSysProp);
        ControllerInfo cInfoXml = getControllerInfoFromXml();
        logger.debug("The resolved properties from Xml are {}", cInfoXml);
        cInfoXml.merge(cInfoSysProp).merge(cInfoYml);
        validateRequiredInfo(cInfoXml);
        if (unresolvedProps != null) {
            logger.error("The following properties {} failed to resolve. Please add them to the 'customDashboard' section in config.yml", unresolvedProps);
            resolved = false;
        } else {
            resolved = true;
        }
        this.dashboardConfig = dashboardConfig;
        this.cInfo = cInfoXml;
        logger.debug("The final resolved properties are {}", cInfo);
    }

    private void validateRequiredInfo(ControllerInfo cInfo) {
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
    }

    protected ControllerInfo getControllerInfoFromXml() {
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

    public String getTierName() {
        return cInfo.getTierName();
    }

    public String getApplicationName() {
        return cInfo.getApplicationName();
    }

    public boolean isResolved() {
        if (!resolved) {
            if (logger.isDebugEnabled()) {
                logger.debug("The final resolved properties are {}", cInfo);
            }
            logger.error("The following properties {} failed to resolve. Please add them to the 'customDashboard' section in config.yml", unresolvedProps);
        }
        return resolved;
    }

    public String getControllerHostName() {
        if (resolved) {
            return cInfo.getControllerHost();
        }
        return null;
    }

    public int getControllerPort() {
        if (resolved) {
            return cInfo.getControllerPort();
        }
        return isControllerUseSSL() ? 443 : 80;
    }

    public boolean isControllerUseSSL() {
        if (resolved) {
            return cInfo.getControllerSslEnabled();
        }
        return false;
    }

    public String getAccountName() {
        if (resolved) {
            return cInfo.getAccount();
        }
        return null;
    }

    public String getUsername() {
        if (resolved) {
            return cInfo.getUsername();
        }
        return null;
    }

    public String getPassword() {
        if (resolved) {
            return CryptoUtil.encode(cInfo.getPassword());
        }
        return null;
    }

}
