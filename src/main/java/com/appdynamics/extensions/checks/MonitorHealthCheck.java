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

import com.appdynamics.extensions.Constants;
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.util.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Satish Muddam
 */
public class MonitorHealthCheck implements Runnable {

    public static Logger logger = null;

    private MonitorExecutorService executorService;

    private LinkedList<Check> healthChecks = new LinkedList<>();

    public MonitorHealthCheck(String monitorName, File installDir, MonitorExecutorService executorService) {
        configureLogger(installDir, monitorName);
        //Do not change this logger
        logger = LoggerFactory.getLogger(monitorName);
        this.executorService = executorService;
    }

    public void updateMonitorExecutorService(MonitorExecutorService executorService) {
        this.executorService = executorService;
    }

    private static void configureLogger(File installDir, String monitorName) {
        try {
            PatternLayout layout = new PatternLayout("%d{ABSOLUTE} %5p [%t] %c{1} - %m%n");
            RollingFileAppender fileAppender = new RollingFileAppender();
            File file = new File(installDir, "logs/monitor-checks/" + monitorName + ".log");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fileAppender.setFile(file.getAbsolutePath(), true, false, 8 * 1024);
            fileAppender.setLayout(layout);
            fileAppender.setMaxBackupIndex(5);
            fileAppender.setMaxFileSize("5MB");

            String property = System.getProperty(Constants.EXTENSIONS_CHECKS_LOG_LEVEL);

            Level level = Level.INFO;
            if (StringUtils.hasText(property)) {
                level = Level.toLevel(property);
            }

            org.apache.log4j.Logger extCheckLogger = org.apache.log4j.Logger.getLogger(monitorName);
            extCheckLogger.setLevel(level);
            extCheckLogger.setAdditivity(false);
            extCheckLogger.addAppender(fileAppender);
        } catch (IOException e) {
            System.out.println("Unable to initialise monitor check logger: " + e.getMessage());
        }
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