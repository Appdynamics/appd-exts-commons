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

import com.appdynamics.extensions.checks.*;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.executorservice.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public class HealthCheckModule {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(HealthCheckModule.class);
    /*
     #TODO @satish.muddam Why is this map needed? We understand that this is caching the MonitorHealthCheck.
     But a concurrentHashMap is not the right data structure. Can't we directly save it as MonitorHealthCheck?
     */
    private Map<String, MonitorHealthCheck> healthChecksForMonitors = new ConcurrentHashMap<>();
    private MonitorExecutorService executorService;

    public void initMATroubleshootChecks(Map<String, ?> config, String monitorName, ControllerInfo controllerInfo, ControllerAPIService controllerAPIService) {
        // #TODO @venkata.konala These checks should not block this. Instead it should log in the health logs in the corresponding check.
        String enableHealthChecksSysPropString = System.getProperty("enableHealthChecks");
        Boolean enableHealthChecksSysProp = true;
        if (enableHealthChecksSysPropString != null) {
            enableHealthChecksSysProp = Boolean.valueOf(enableHealthChecksSysPropString);
        }
        Boolean enableHealthChecks = (Boolean) config.get("enableHealthChecks");
        if (enableHealthChecks == null && !enableHealthChecksSysProp) {
            enableHealthChecks = false;
        }

        if (enableHealthChecks != null && !enableHealthChecks) {
            logger.info("Not initializing extension health checks as it is disabled in config");
            return;
        } else {
            logger.info("Running extension health checks");
        }
        if (monitorName == null) {
            logger.warn("Not initializing extension health checks as I have no idea on what extension is running now");
            return;
        }
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        /**
         *  Initializing the thread pool with 2 threads.
         *    one thread will be used for all the normal checks
         *    second thread will be used for run-always check
         *
         **/
        //#TODO @satish.muddam The size has to be 1 + number of runAlwaysChecks. Already changed it from 2 to 3. Please validate.
        executorService = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(3));
        try {
            File installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
            MonitorHealthCheck healthCheckMonitor = healthChecksForMonitors.get(monitorName);
            if (healthCheckMonitor == null) {
                healthCheckMonitor = new MonitorHealthCheck(monitorName, installDir, executorService);
                healthChecksForMonitors.put(monitorName, healthCheckMonitor);
            } else {
                healthCheckMonitor.updateMonitorExecutorService(executorService);
                healthCheckMonitor.clearAllChecks();
            }
            healthCheckMonitor.registerChecks(new AppTierNodeCheck(controllerInfo, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MaxMetricLimitCheck(20, TimeUnit.SECONDS, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MetricBlacklistLimitCheck(20, TimeUnit.SECONDS, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MachineAgentAvailabilityCheck(controllerInfo, controllerAPIService, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new ExtensionPathConfigCheck(controllerInfo, Collections.unmodifiableMap(config), controllerAPIService, MonitorHealthCheck.logger));
            executorService.submit("HealthCheckMonitor", healthCheckMonitor);

        } catch (Exception e) {
            logger.error("Error initializing health check module", e);
        }
    }
}
