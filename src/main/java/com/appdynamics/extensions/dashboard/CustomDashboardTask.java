package com.appdynamics.extensions.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardTask {
    public static final Logger logger = LoggerFactory.getLogger(CustomDashboardTask.class);

    private CustomDashboardGenerator dashboardGenerator;
    private long nextRunTime;
    private long frequency = 5 * 60 * 1000;

    public void updateConfig(Set<String> instanceNames, String metricPrefix, Map dashboardConfig) {
        dashboardGenerator = new CustomDashboardGenerator(instanceNames, metricPrefix, dashboardConfig);
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
            if (current + 2 * 60 * 10000 > nextRunTime) {
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
