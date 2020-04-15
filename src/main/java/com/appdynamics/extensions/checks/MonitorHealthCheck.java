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

import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedList;

/**
 * @author Satish Muddam
 */
public class MonitorHealthCheck implements Runnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MonitorHealthCheck.class);

    private MonitorExecutorService executorService;

    private LinkedList<Check> healthChecks = new LinkedList<>();

    public MonitorHealthCheck(MonitorExecutorService executorService) {
        this.executorService = executorService;
    }

    public void updateMonitorExecutorService(MonitorExecutorService executorService) {
        this.executorService = executorService;
    }

    public void registerChecks(Check healthCheck) {
        if (healthCheck != null) {
            healthChecks.add(healthCheck);
        }
    }

    public void clearAllChecks() {
        if (healthChecks != null) {
            healthChecks.clear();
        }
    }

    private void validate() {

        logger.info("Running monitor health checks");

        for (final Check check : healthChecks) {
            if (check instanceof RunOnceCheck) {
                try {
                    check.check();
                } catch (Exception e) {
                    logger.error(" Exception when running {} ", check, e);
                }

            } else if (check instanceof RunAlwaysCheck) {

                final RunAlwaysCheck runAlwaysCheck = (RunAlwaysCheck) check;

                executorService.scheduleAtFixedRate("RunAlwaysCheck", new Runnable() {
                    @Override
                    public void run() {
                        try {
                            runAlwaysCheck.check();
                        } catch (Exception e) {
                            logger.error(" Exception when running {} ", runAlwaysCheck, e);
                        }
                    }
                }, 0, runAlwaysCheck.getPeriod(), runAlwaysCheck.getTimeUnit());

            } else {
                //Nothing matched
            }
        }
    }

    @Override
    public void run() {
        validate();
    }
}