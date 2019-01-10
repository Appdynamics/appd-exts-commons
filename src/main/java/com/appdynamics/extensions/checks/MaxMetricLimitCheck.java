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
        if (!stop) {
            long start = System.currentTimeMillis();
            logger.info("Starting MaxMetricLimitCheck");
            File directory = PathResolver.resolveDirectory(AManagedMonitor.class);
            if (directory.exists()) {
                File logs = new File(directory, "logs");
                //This is taking around 500ms for 25MB log files ( 5 files * 5 MB )
                //#TODO @satish.muddam Add support for windows : https://www.baeldung.com/grep-in-java
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
        } else {
            logger.info("Metric limit is reached and the message logged. Not executing MaxMetricLimitCheck.");
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
