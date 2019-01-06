/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.apiservices.ApplicationModelAPIService;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author Satish Muddam
 */
//#TODO Handle the standalone MA with multiple app agents case after the Full Agent Resolver is implemented and
// there is a flag in ControllerInfo to know this special case. This flag would indicate that the app, tier and node in the
// ControllerInfo are not the only ones to which MA sends the data to. So in this case the customers have to use
// "Custom Metrics| Extension"
public class ExtensionPathConfigCheck implements RunOnceCheck {

    public Logger logger;
    private static final Escaper URL_ESCAPER = UrlEscapers.urlFragmentEscaper();
    private ControllerInfo controllerInfo;
    private Map<String, ?> config;
    private ApplicationModelAPIService applicationModelAPIService;

    public ExtensionPathConfigCheck(ControllerInfo controllerInfo, Map<String, ?> config, ApplicationModelAPIService applicationModelAPIService, Logger logger) {
        this.logger = logger;
        this.controllerInfo = controllerInfo;
        this.config = config;
        this.applicationModelAPIService = applicationModelAPIService;
    }

    @Override
    public void check() {
        AssertUtils.assertNotNull(this.applicationModelAPIService, "The ApplicationModelAPIService is null");
        long start = System.currentTimeMillis();
        logger.info("Starting ExtensionPathConfigCheck");
        if (controllerInfo == null) {
            logger.error("Received ControllerInfo as null. Not checking anything.");
            return;
        }
        //#TODO @satish.muddam Get it from MonitorContext, not directly from config.yml
        String metricPrefix = (String) config.get("metricPrefix");
        //#TODO @satish.muddam If getting the metricPrefix from MonitorContext, then the following check is not required.
        if (Strings.isNullOrEmpty(metricPrefix)) {
            logger.error("Metric prefix not configured in config file");
            return;
        }
        if (metricPrefix.startsWith("Server|Component")) { //Tier is configured in metric prefix
            if (controllerInfo.getSimEnabled()) {
                logger.error("No need to configure tier-id as SIM is enabled. Please use the alternate metric prefix.");
            } else {
                String[] split = metricPrefix.split(":|\\|");
                String extensionTier = split[2];
                String maTierID = getMAConfiguredTier();
                if (maTierID != null) {
                    if (extensionTier.equals(maTierID) || extensionTier.equals(controllerInfo.getTierName())) {
                        logger.info("Extension configured correct tier id/tier name");
                    } else {
                        logger.error("Extension did not configure correct tier. Tier to configure [" + controllerInfo.getTierName() +
                                "] with tier id [" + maTierID + "], but configured tier [" + extensionTier + "]");
                    }
                }
            }
        } else { //Tier is not configured in metric prefix
            if (!controllerInfo.getSimEnabled()) {
                logger.info("Configured metric prefix with no tier id. With this configuration, metric browser will show metric names in all the available tiers/applications(when there are multiple app agents)");
            } else {
                logger.info("SIM is enabled, please look in the SIM metric browser for metrics.");
            }
        }
        long diff = System.currentTimeMillis() - start;
        logger.info("ExtensionPathConfigCheck took {} ms to complete ", diff);
    }

    private String getMAConfiguredTier() {
        JsonNode jsonNode = applicationModelAPIService.getSpecificTierNode(controllerInfo.getApplicationName(), controllerInfo.getTierName());
        if(jsonNode != null) {
            return jsonNode.get(0).get("id").asText();
        }
        return null;
    }
}