package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public class HealthCheckModule {

    public static final Logger logger = LoggerFactory.getLogger(HealthCheckModule.class);


    public void initMATroubleshootChecks(String monitorName, File installDir, Map<String, ?> config) {

        Boolean enableHealthChecks = (Boolean) config.get("enableHealthChecks");

        if (enableHealthChecks == null || !enableHealthChecks) {
            logger.info("Not initializing extension health checks as it is disabled in config");
            return;
        }

        if (monitorName == null) {
            logger.warn("Not initializing extension health checks as I have no idea on what extension is running now");
            return;
        }

        try {
            MonitorHealthCheck healthCheckMonitor = new MonitorHealthCheck(monitorName, installDir);
            ControllerInfo controllerInfo = healthCheckMonitor.init();

            ControllerRequestHandler controllerRequestHandler = new ControllerRequestHandler(controllerInfo, MonitorHealthCheck.logger);

            healthCheckMonitor.registerChecks(new AppTierNodeCheck(controllerInfo, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MaxMetricLimitCheck(20, TimeUnit.SECONDS, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new MachineAgentAvailabilityCheck(controllerInfo, controllerRequestHandler, MonitorHealthCheck.logger));
            healthCheckMonitor.registerChecks(new ExtensionPathConfigCheck(controllerInfo, Collections.unmodifiableMap(config), controllerRequestHandler, MonitorHealthCheck.logger));

            //#TODO use monitorexecutorservice...
            Executors.newSingleThreadExecutor().submit(healthCheckMonitor);
        } catch (Exception e) {
            logger.error("Error initializing health check module", e);
        }
    }
}
