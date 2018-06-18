/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import com.google.common.base.Strings;
import org.slf4j.Logger;

/**
 * @author Satish Muddam
 */
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

        if (Strings.isNullOrEmpty(controllerInfo.getApplicationName()) || Strings.isNullOrEmpty(controllerInfo.getTierName())
                || Strings.isNullOrEmpty(controllerInfo.getNodeName())) {

            if (!simEnabled) {
                logger.error("SIM is not enabled and Application name, Tier name or node name not configured. " +
                        "For more details, please visit https://docs.appdynamics.com");
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