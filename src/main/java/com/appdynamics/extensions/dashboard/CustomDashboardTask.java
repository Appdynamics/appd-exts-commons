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
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by abey.tom on 4/11/15.
 */

public class CustomDashboardTask {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardTask.class);

    private CustomDashboardGenerator dashboardGenerator;
    private long nextRunTime;
    private long frequency = 5 * 60 * 1000;

    public void updateConfig(Set<String> instanceNames, String metricPrefix, Map dashboardConfig, ControllerInfo controllerInfo) {
        dashboardGenerator = new CustomDashboardGenerator(instanceNames, metricPrefix, dashboardConfig, controllerInfo);
        if (dashboardConfig != null) {
            Integer freqStr = (Integer) dashboardConfig.get("executionFrequencyMinutes");
            if (freqStr != null) {
                frequency = freqStr.longValue() * 60 * 1000;
            }
            logger.debug("The execution frequency of CustomDashboardTask is set to {} seconds", (int) (frequency / 1000));
        }
        nextRunTime = System.currentTimeMillis() + frequency;
    }

    public void run(Collection<String> metrics) {
        if (dashboardGenerator != null) {
            long current = System.currentTimeMillis();
            //if it is a 2 second diff, then also run it.
            if (current + 3 * 1000 > nextRunTime) {
                try {
                    dashboardGenerator.createDashboards(metrics);
                } catch (Exception e) {
                    logger.error("Error while generating the custom dashboard", e);
                } finally {
                    nextRunTime = current + frequency;
                }
            } else {
                logger.debug("{} seconds to go before the next run", (long) ((nextRunTime - current) / 1000D));
            }
        } else {
            logger.warn("The method updateConfig is not invoked. Please invoke it before invoking the run method");
        }
    }
}
