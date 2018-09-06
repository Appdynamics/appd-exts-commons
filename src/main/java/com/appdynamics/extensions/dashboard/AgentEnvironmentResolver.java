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

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.conf.ControllerInfoFactory;
import com.appdynamics.extensions.conf.ControllerInfoValidator;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Created by abey.tom on 4/10/15.
 */
public class AgentEnvironmentResolver {
    public static final org.slf4j.Logger logger = ExtensionsLoggerFactory.getLogger(AgentEnvironmentResolver.class);
    private ControllerInfo cInfo;
    private boolean resolved;

    // TODO Find a good name for this class, remove the getters
    // create a new class and extend from the base agent env class
    AgentEnvironmentResolver(ControllerInfo controllerInfo) {

        this.cInfo = controllerInfo;
        ControllerInfoValidator validator = new ControllerInfoValidator();
        resolved = validator.validateAndCheckIfResolved(cInfo);
    }

    public String getTierName() {
        return cInfo.getTierName();
    }

    public String getNodeName() {
        return cInfo.getNodeName();
    }

    public String getApplicationName() {
        return cInfo.getApplicationName();
    }

    public boolean isResolved() {
        if (!resolved) {
            if (logger.isDebugEnabled()) {
                logger.debug("The final resolved properties are {}", cInfo);
            }
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

    public Boolean getSimEnabled() {
        if (resolved) {
            return cInfo.getSimEnabled();
        }
        return null;
    }

    public String getMachinePath() {
        if (resolved) {
            return cInfo.getMachinePath();
        }
        return null;
    }

}
