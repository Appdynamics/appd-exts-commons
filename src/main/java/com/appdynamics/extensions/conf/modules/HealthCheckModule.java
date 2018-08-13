/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.checks.AppTierNodeCheck;
import com.appdynamics.extensions.checks.ControllerRequestHandler;
import com.appdynamics.extensions.checks.ExtensionPathConfigCheck;
import com.appdynamics.extensions.checks.MachineAgentAvailabilityCheck;
import com.appdynamics.extensions.checks.MaxMetricLimitCheck;
import com.appdynamics.extensions.checks.MonitorHealthCheck;
import com.appdynamics.extensions.conf.ControllerInfo;
import com.appdynamics.extensions.conf.ControllerInfoFactory;
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
    private Map<String, MonitorHealthCheck> healthChecksForMonitors = new ConcurrentHashMap<>();
    private MonitorExecutorService executorService;


    public void initMATroubleshootChecks(ControllerInfo controllerInfo,String monitorName, Map<String, ?> config) {

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

        if (!validateControllerInfo(controllerInfo)) {
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
        executorService = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(2));

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

            ControllerRequestHandler controllerRequestHandler = new ControllerRequestHandler(controllerInfo, MonitorHealthCheck.logger);

            healthCheckMonitor.registerChecks(new AppTierNodeCheck(controllerInfo, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MaxMetricLimitCheck(20, TimeUnit.SECONDS, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MachineAgentAvailabilityCheck(controllerInfo, controllerRequestHandler, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new ExtensionPathConfigCheck(controllerInfo, Collections.unmodifiableMap(config), controllerRequestHandler, MonitorHealthCheck.logger));


            executorService.submit("HealthCheckMonitor", healthCheckMonitor);

        } catch (Exception e) {
            logger.error("Error initializing health check module", e);
        }
    }

    private boolean validateControllerInfo(ControllerInfo controllerInfo) {

        if (controllerInfo == null) {
            return false;
        }

        if(controllerInfo.getControllerHost() == null || controllerInfo.getControllerPort() == null
                || controllerInfo.getControllerSslEnabled() == null || controllerInfo.getSimEnabled() == null) {
            return false;
        }
        return true;
    }

}
