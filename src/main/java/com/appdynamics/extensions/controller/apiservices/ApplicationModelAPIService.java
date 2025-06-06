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

package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Created by venkata.konala on 1/1/19.
 */
public class ApplicationModelAPIService extends APIService {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(ApplicationModelAPIService.class);

    ApplicationModelAPIService(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        super(controllerInfo, controllerClient);
    }

    public JsonNode getSpecificTierNode(String applicationName, String tierName) {
        if (controllerClient != null) {
            JsonNode specificTierNode = null;
            String specificTierData;
            try {
                StringBuilder sb = new StringBuilder("controller/rest/applications/");
                sb.append(applicationName)
                        .append("/tiers/").append(tierName)
                        .append("?output=JSON");
                specificTierData = controllerClient.sendGetRequest(sb.toString());
                specificTierNode = new ObjectMapper().readTree(specificTierData);
            } catch (ControllerHttpRequestException e) {
                logger.error("Invalid response from controller while fetching information about all dashbards", e);
            } catch (IOException e) {
                logger.error("Error while getting all dashboards information", e);
            }
            return specificTierNode;
        }
        logger.debug("The controllerClient is not initialized");
        return null;
    }
}
