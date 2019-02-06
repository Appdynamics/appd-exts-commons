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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.controller.*;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIServiceFactory;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static com.appdynamics.extensions.Constants.ENCRYPTION_KEY;

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerModule.class);
    private ControllerInfo controllerInfo;
    private ControllerClient controllerClient;
    private ControllerAPIService controllerAPIService;

    /**
     * This method initializes the ControllerInfo instance which is singleton from the ControllerInfoFactory and then
     * validates it and uses the ControllerInfo instance to create a HttpClient to the Controller, viz. ControllerClient
     * @param installDir    It refers to the machine agent home folder.
     * @param config        The controllerInfo section form the config.yml resolved as a map
     *
     * */
    public void initController(File installDir, Map<String, ?> config) {
        Map controllerInfoMap = (Map) config.get("controllerInfo");
        if(controllerInfoMap == null) {
            controllerInfoMap = Maps.newHashMap();
        }
        controllerInfoMap.put(ENCRYPTION_KEY, config.get(ENCRYPTION_KEY));
        try {
            controllerInfo = ControllerInfoFactory.initialize(controllerInfoMap, installDir);
            logger.info("Initialized ControllerInfo");
            ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
            if (controllerInfoValidator.isValidated()) {
                controllerClient = ControllerClientFactory.initialize(controllerInfo,
                        (Map<String, ?>) config.get("connection"), (Map<String, ?>) config.get("proxy"),
                        (String) config.get(ENCRYPTION_KEY));
                logger.debug("Initialized ControllerClient");
                controllerAPIService = ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
                logger.debug("Initialized ControllerAPIService");
                return;
            }
            logger.warn("ControllerInfo instance is not validated and resolved.....the ControllerClient and ControllerAPIService are null");
        } catch (Exception e) {
            logger.error("Unable to initialize the ControllerModule properly.....the ControllerClient and ControllerAPIService will be set to null", e);
        }
        //#TODO Check if the following is ok.
        controllerClient = null;
        controllerAPIService = null;
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    public ControllerClient getControllerClient() {
        return controllerClient;
    }

    public ControllerAPIService getControllerAPIService() {
        return controllerAPIService;
    }
 }
