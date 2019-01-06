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
 * @author Akshay Srivastava
 */

//#TODO @satish.muddam This healthcheck can be combined with MaxMetricLimitChack, using a single Pattern.
public class MetricBlacklistLimitCheck implements RunAlwaysCheck {

    public Logger logger;

    private static final String BLACKLIST_METRIC_LIMIT_ERROR_LINE = "WARN ManagedMonitorDelegate - Metric registration blacklist limit reached";

    private long period;
    private TimeUnit timeUnit;
    private boolean stop = false;

    public MetricBlacklistLimitCheck(long period, TimeUnit timeUnit, Logger logger) {
        this.logger = logger;
        this.period = period;
        this.timeUnit = timeUnit;
    }


    @Override
    public void check() {
        if (!stop) {
            long start = System.currentTimeMillis();
            logger.info("Starting MetricBlacklistLimitCheck");
            File directory = PathResolver.resolveDirectory(AManagedMonitor.class);
            if (directory.exists()) {
                File logs = new File(directory, "logs");
                //This is taking around 500ms for 25MB log files ( 5 files * 5 MB )
                List<Line> blacklistLimitErrorLogLines = Unix4j.cd(logs).grep(Grep.Options.lineNumber, BLACKLIST_METRIC_LIMIT_ERROR_LINE, "*.log*").toLineList();
                if (blacklistLimitErrorLogLines != null && blacklistLimitErrorLogLines.size() > 0) {
                    logger.error("Found blacklist metric limit reached error, below are the details");
                    for (Line line : blacklistLimitErrorLogLines) {
                        logger.error(line.toString());
                    }
                    stop = true;
                }
            }
            long diff = System.currentTimeMillis() - start;
            logger.info("MetricBlacklistLimitCheck took {} ms to complete ", diff);
        } else {
            logger.info("Blacklist Metric limit is reached and the message logged. Not executing MetricBlacklistLimitCheck.");
        }
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
