/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.util.JsonUtils;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * @author Satish Muddam
 */
public class MachineAgentAvailabilityCheck implements RunOnceCheck {
    public Logger logger;

    private ControllerInfo controllerInfo;
    private ControllerRequestHandler controllerRequestHandler;

    private static final Escaper URL_ESCAPER = UrlEscapers.urlFragmentEscaper();


    public MachineAgentAvailabilityCheck(ControllerInfo controllerInfo, ControllerRequestHandler controllerRequestHandler, Logger logger) {
        this.logger = logger;
        this.controllerInfo = controllerInfo;
        this.controllerRequestHandler = controllerRequestHandler;
    }

    @Override
    public void check() {

        long start = System.currentTimeMillis();

        logger.info("Starting MachineAgentAvailabilityCheck");


        if (controllerInfo == null) {
            logger.error("Received ControllerInfo as null. Not checking anything.");
            return;
        }

        if (controllerInfo.getSimEnabled()) {
            logger.info("SIM is enabled, not checking MachineAgent availability metric");
            //TODO: Check if MA status needs to be verified if SIM is enabled.
            return;
        }

        int maStatus = getMAStatus();
        if (maStatus == 1) {
            logger.info("MachineAgent is reporting availability metric");
        } else {
            logger.error("MachineAgent is not reporting availability metric. Please check your configuration");
        }

        long diff = System.currentTimeMillis() - start;
        logger.info("MachineAgentAvailabilityCheck took {} ms to complete ", diff);
    }

    private int getMAStatus() {

        try {

            String statusURL = buildMAStatusCheckURL();
            String responseString = controllerRequestHandler.sendGet(statusURL);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseString);

            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            return valueNode.get(0).asInt();
        } catch (InvalidResponseException e) {
            logger.error("Invalid response from controller while fetching MA status", e);

        } catch (IOException e) {
            logger.error("Error while getting the MA status information", e);
        }
        return 0;
    }

    private String buildMAStatusCheckURL() {
        StringBuilder sb = new StringBuilder("/controller/rest/applications/");
        sb.append(controllerInfo.getApplicationName())
                .append("/metric-data?metric-path=Application Infrastructure Performance|")
                .append(controllerInfo.getTierName()).append("|Agent|Machine|Availability&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
        return URL_ESCAPER.escape(sb.toString());
    }
}