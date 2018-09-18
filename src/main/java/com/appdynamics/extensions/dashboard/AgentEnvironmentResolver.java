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

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoValidator;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;

/**
 * Created by abey.tom on 4/10/15.
 */
public class AgentEnvironmentResolver {
    public static final org.slf4j.Logger logger = ExtensionsLoggerFactory.getLogger(AgentEnvironmentResolver.class);
    private ControllerInfo cInfo;
    private boolean resolved;

    AgentEnvironmentResolver(ControllerInfo controllerInfo) {

        this.cInfo = controllerInfo;
        ControllerInfoValidator validator = new ControllerInfoValidator();
        resolved = validator.isValidatedAndResolved(cInfo);
    }

    public boolean isResolved() {
        if (!resolved) {
            if (logger.isDebugEnabled()) {
                logger.debug("The final resolved properties are {}", cInfo);
            }
        }
        return resolved;
    }


}
