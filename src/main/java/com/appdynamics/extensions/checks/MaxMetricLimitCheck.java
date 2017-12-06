package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.unix4j.Unix4j;
import org.unix4j.line.Line;
import org.unix4j.unix.Grep;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public class MaxMetricLimitCheck implements RunAlwaysCheck {

    public Logger logger;

    private static final String MAX_METRIC_ERROR_LINE = "ERROR ManagedMonitorDelegate - Maximum metrics limit reached";

    private long period;
    private TimeUnit timeUnit;
    private boolean stop = false;

    public MaxMetricLimitCheck(long period, TimeUnit timeUnit, Logger logger) {
        this.logger = logger;
        this.period = period;
        this.timeUnit = timeUnit;
    }


    @Override
    public void check() {

        long start = System.currentTimeMillis();

        logger.info("Starting MaxMetricLimitCheck");

        File directory = PathResolver.resolveDirectory(AManagedMonitor.class);
        if (directory.exists()) {
            File logs = new File(directory, "logs");
            List<Line> metricLimitErrorLogLines = Unix4j.cd(logs).grep(Grep.Options.lineNumber, MAX_METRIC_ERROR_LINE, "*.log*").toLineList();

            if (metricLimitErrorLogLines != null && metricLimitErrorLogLines.size() > 0) {

                logger.error("Found metric limit reached error, below are the details");
                for (Line line : metricLimitErrorLogLines) {
                    logger.error(line.toString());
                }
                stop = true;
            }
        }

        long diff = System.currentTimeMillis() - start;
        logger.info("MaxMetricLimitCheck took {} ms to complete ", diff);
    }

    @Override
    public long getPeriod() {
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
