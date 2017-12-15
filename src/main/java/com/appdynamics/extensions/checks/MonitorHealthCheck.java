package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Satish Muddam
 */
public class MonitorHealthCheck implements Runnable {

    public static final String EXTENSIONS_CHECKS_LOG_LEVEL = "monitor.checks.log.level";
    public static Logger logger = null;

    private LinkedList<Check> healthChecks = new LinkedList<>();

    public MonitorHealthCheck(String monitorName, File installDir) {
        configureLogger(installDir, monitorName);
        logger = LoggerFactory.getLogger(monitorName);
    }

    private static void configureLogger(File installDir, String extensionName) {
        try {
            PatternLayout layout = new PatternLayout("%d{ABSOLUTE} %5p [%t] %c{1} - %m%n");
            RollingFileAppender fileAppender = new RollingFileAppender();
            File file = new File(installDir, "logs/monitor-checks/" + extensionName + ".log");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fileAppender.setFile(file.getAbsolutePath(), true, false, 8 * 1024);
            fileAppender.setLayout(layout);
            fileAppender.setMaxBackupIndex(5);
            fileAppender.setMaxFileSize("5MB");

            String property = System.getProperty(EXTENSIONS_CHECKS_LOG_LEVEL);

            Level level = Level.INFO;
            if (StringUtils.hasText(property)) {
                level = Level.toLevel(property);
            }

            org.apache.log4j.Logger extCheckLogger = org.apache.log4j.Logger.getLogger(extensionName);
            extCheckLogger.setLevel(level);
            extCheckLogger.setAdditivity(false);
            extCheckLogger.addAppender(fileAppender);
        } catch (IOException e) {
            System.out.println("Unable to initialise monitor check logger: " + e.getMessage());
        }
    }

    public ControllerInfo init() {

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

    public void registerChecks(Check healthCheck) {
        healthChecks.add(healthCheck);
    }

    private void validate() {

        for (final Check check : healthChecks) {
            if (check instanceof RunOnceCheck) {

                check.check();

            } else if (check instanceof RunAlwaysCheck) {

                final RunAlwaysCheck runAlwaysCheck = (RunAlwaysCheck) check;
                //#TODO use the MonitorExecutorService....
                final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {

                        runAlwaysCheck.check();

                        if (runAlwaysCheck.shouldStop()) {
                            scheduledExecutorService.shutdownNow();
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