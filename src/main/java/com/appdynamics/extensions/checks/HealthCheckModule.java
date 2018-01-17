package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.dashboard.ControllerInfo;
import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final Logger logger = LoggerFactory.getLogger(HealthCheckModule.class);
    private Map<String, MonitorHealthCheck> healthChecksForMonitors = new ConcurrentHashMap<>();
    private ControllerInfo controllerInfo;

    public HealthCheckModule() {
        controllerInfo = getControllerInfo();
    }


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

            MonitorHealthCheck healthCheckMonitor = healthChecksForMonitors.get(monitorName);
            if (healthCheckMonitor == null) {
                healthCheckMonitor = new MonitorHealthCheck(monitorName, installDir);
                healthChecksForMonitors.put(monitorName, healthCheckMonitor);

                ControllerRequestHandler controllerRequestHandler = new ControllerRequestHandler(controllerInfo, MonitorHealthCheck.logger);

                healthCheckMonitor.registerChecks(new AppTierNodeCheck(controllerInfo, MonitorHealthCheck.logger));
                healthCheckMonitor.registerChecks(new MaxMetricLimitCheck(20, TimeUnit.SECONDS, MonitorHealthCheck.logger));
                healthCheckMonitor.registerChecks(new MachineAgentAvailabilityCheck(controllerInfo, controllerRequestHandler, MonitorHealthCheck.logger));
                healthCheckMonitor.registerChecks(new ExtensionPathConfigCheck(controllerInfo, Collections.unmodifiableMap(config), controllerRequestHandler, MonitorHealthCheck.logger));
            }

            MonitorExecutorService executorService = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(1));
            executorService.submit("HealthCheckMonitor", healthCheckMonitor);

        } catch (Exception e) {
            logger.error("Error initializing health check module", e);
        }
    }

    private ControllerInfo getControllerInfo() {

        ControllerInfo controllerInfoFromSystemProps = ControllerInfo.fromSystemProperties();
        ControllerInfo controllerInfoFromXml = getControllerInfoFromXml();
        ControllerInfo controllerInfo = controllerInfoFromXml.merge(controllerInfoFromSystemProps);

        return controllerInfo;
    }

    private ControllerInfo getControllerInfoFromXml() {
        File directory = PathResolver.resolveDirectory(AManagedMonitor.class);
        logger.info("The install directory is resolved to {}", directory.getAbsolutePath());
        ControllerInfo from = null;
        if (directory.exists()) {
            File cinfo = new File(new File(directory, "conf"), "controller-info.xml");
            if (cinfo.exists()) {
                from = ControllerInfo.fromXml(cinfo);
            }
        }
        if (from == null) {
            from = new ControllerInfo();
        }
        return from;
    }
}
