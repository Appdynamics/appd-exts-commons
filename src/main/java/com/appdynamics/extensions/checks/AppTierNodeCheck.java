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

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.controller.ControllerInfo;
import com.google.common.base.Strings;
import org.slf4j.Logger;

/**
 * @author Satish Muddam
 */
// #TODO If the FullAgentResolver is implemented, then we are enforcing the proper configuration by making the extension
// fail. In that case this check is not required.
public class AppTierNodeCheck implements RunOnceCheck {

    public Logger logger;

    private ControllerInfo controllerInfo;

    public AppTierNodeCheck(ControllerInfo controllerInfo, Logger logger) {
        this.controllerInfo = controllerInfo;
        this.logger = logger;
    }

    @Override
    public void check() {

        long start = System.currentTimeMillis();

        logger.info("Starting AppTierNodeCheck");

        if (controllerInfo == null) {
            logger.error("Received ControllerInfo as null. Not checking anything.");
            return;
        }

        Boolean simEnabled = controllerInfo.getSimEnabled();
        //#TODO @satish.muddam Nodename is not mandatory. defaults to node1. Please check.
        if (Strings.isNullOrEmpty(controllerInfo.getApplicationName()) || Strings.isNullOrEmpty(controllerInfo.getTierName())
                || Strings.isNullOrEmpty(controllerInfo.getNodeName())) {

            if (!simEnabled) {
                logger.error("SIM is not enabled and Application name, Tier name or node name not resolved");
            } else {
                logger.info("Application name, Tier name or node name not configured but SIM is enabled. Check for metrics in the SIM metric browser.");
            }
        } else {
            logger.info("Application name [{}], Tier name [{}] and node name [{}] are configured", controllerInfo.getApplicationName(),
                    controllerInfo.getTierName(), controllerInfo.getNodeName());
            logger.info("SIM is [{}]", simEnabled ? " Enabled " : " Not Enabled ");
        }


        long diff = System.currentTimeMillis() - start;
        logger.info("AppTierNodeCheck took {} ms to complete ", diff);
    }
}