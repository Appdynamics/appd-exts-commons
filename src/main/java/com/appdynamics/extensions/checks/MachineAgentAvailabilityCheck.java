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
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.JsonUtils;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public class MachineAgentAvailabilityCheck implements RunAlwaysCheck {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MachineAgentAvailabilityCheck.class);

    private ControllerInfo controllerInfo;
    private ControllerAPIService controllerAPIService;
    private MetricAPIService metricAPIService;

    private int period;
    private TimeUnit timeUnit;
    private boolean stop = false;

    private static final Escaper URL_ESCAPER = UrlEscapers.urlFragmentEscaper();


    public MachineAgentAvailabilityCheck(ControllerInfo controllerInfo, ControllerAPIService controllerAPIService, int period, TimeUnit timeUnit) {
        this.controllerInfo = controllerInfo;
        this.controllerAPIService = controllerAPIService;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    @Override
    public void check() {
        if(!stop) {
            long start = System.currentTimeMillis();
            logger.info("Starting MachineAgentAvailabilityCheck");
            if (controllerInfo == null) {
                logger.error("Received ControllerInfo as null. Not checking anything.");
                return;
            }
            AssertUtils.assertNotNull(controllerAPIService, "The ControllerAPIService is null");
            metricAPIService = controllerAPIService.getMetricAPIService();
            AssertUtils.assertNotNull(metricAPIService, "The MetricAPIService is null");
            if (controllerInfo.getSimEnabled()) {
                logger.info("SIM is enabled, not checking MachineAgent availability metric");
                //TODO @satish.muddam Check if MA status needs to be verified if SIM is enabled.
                return;
            }
            int maStatus = getMAStatus();
            if (maStatus == 1) {
                logger.info("MachineAgent is reporting availability metric");
                stop = true;
            } else {
                logger.error("MachineAgent is not reporting availability metric. Please check your configuration");
            }
            long diff = System.currentTimeMillis() - start;
            logger.info("MachineAgentAvailabilityCheck took {} ms to complete ", diff);
        } else {
            logger.info("Machine agent availability metric reported");
        }
    }

    private int getMAStatus() {
        JsonNode jsonNode = metricAPIService.getMetricData(controllerInfo.getApplicationName(), getEndPointForMAStatusMetric());
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            return valueNode == null ? 0 : valueNode.get(0).asInt();
        } else {
            return 0;
        }
    }

    //#TODO @venkata.konala Check if this end point is still valid in the latest versions of the controller
    private String getEndPointForMAStatusMetric() {
        StringBuilder sb = new StringBuilder();
        sb.append("/metric-data?metric-path=Application Infrastructure Performance|")
                .append(controllerInfo.getTierName()).append("|Agent|Machine|Availability&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
        return URL_ESCAPER.escape(sb.toString());
    }

    @Override
    public int getPeriod() {
        return period;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public boolean shouldStop() {
        return stop;
    }
}