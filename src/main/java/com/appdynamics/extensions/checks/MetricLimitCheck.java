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

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.unix4j.Unix4j;
import org.unix4j.line.Line;
import org.unix4j.unix.Grep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public class MetricLimitCheck implements RunAlwaysCheck {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricLimitCheck.class);

    private static final String MAX_METRIC_ERROR_LINE = "ERROR ManagedMonitorDelegate - Maximum metrics limit reached";
    private static final String BLACKLIST_METRIC_LIMIT_ERROR_LINE = "WARN ManagedMonitorDelegate - Metric registration blacklist limit reached";


    private int period;
    private TimeUnit timeUnit;
    private boolean stop = false;

    public MetricLimitCheck(int period, TimeUnit timeUnit) {
        this.period = period;
        this.timeUnit = timeUnit;
    }


    @Override
    public void check() {
        if (!stop) {
            long start = System.currentTimeMillis();
            logger.info("Starting MetricLimitCheck");
            File directory = PathResolver.resolveDirectory(AManagedMonitor.class);
            if (directory.exists()) {
                File logs = new File(directory, "logs");
                //This is taking around 500ms for 25MB log files ( 5 files * 5 MB )
                List<Line> metricLimitErrorLogLines = new ArrayList<>();
                List<Line> maxMetricLimitErrors = Unix4j.cd(logs).grep(Grep.Options.lineNumber, MAX_METRIC_ERROR_LINE, "*.log*").toLineList();

                List<Line> metricBlackListErrors = Unix4j.cd(logs).grep(Grep.Options.lineNumber, BLACKLIST_METRIC_LIMIT_ERROR_LINE, "*.log*").toLineList();

                metricLimitErrorLogLines.addAll(maxMetricLimitErrors);
                metricLimitErrorLogLines.addAll(metricBlackListErrors);

                if (metricLimitErrorLogLines != null && metricLimitErrorLogLines.size() > 0) {
                    logger.error("Found metric limit reached error, below are the details");
                    for (Line line : metricLimitErrorLogLines) {
                        logger.error(line.toString());
                    }
                    stop = true;
                }
            }
            long diff = System.currentTimeMillis() - start;
            logger.info("MetricLimitCheck took {} ms to complete ", diff);
        } else {
            logger.info("Metric limit is reached and the message logged. Not executing MetricLimitCheck.");
        }
    }

    @Override
    public int getPeriod() {
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
