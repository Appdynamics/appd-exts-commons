/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

package com.appdynamics.extensions;


import com.appdynamics.extensions.metrics.Metric;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/*
 *Job represents a single run of the extension
*/

public class AMonitorJob implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(AMonitorJob.class);
    private ABaseMonitor baseMonitor;


    public AMonitorJob(ABaseMonitor baseMonitor) {
        this.baseMonitor = baseMonitor;
    }

    @Override
    public void run() {
        logger.debug("Monitor {} Task Runner invoked",baseMonitor.getMonitorName());
        TasksExecutionServiceProvider obj = new TasksExecutionServiceProvider(baseMonitor, MetricWriteHelperFactory.create(baseMonitor));
        baseMonitor.doRun(obj);
    }

    public void printAllFromCache() {
        ConcurrentMap<String, Metric> map = baseMonitor.configuration.getCachedMetrics();
        Set<String> keys;
        if (map != null && (keys = map.keySet()) != null) {
            for (String key : keys) {
                Metric metric = map.get(key);
                MetricWriter writer = baseMonitor.getMetricWriter(metric.getMetricPath(), metric.getAggregationType(), metric.getTimeRollUpType(), metric.getClusterRollUpType());
                logger.debug("Printing Metric {}", metric);
                writer.printMetric(metric.getMetricValue());
            }
        } else {
            logger.info("The Metric Cache is empty, no values are present");
        }
    }
}
