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

package com.appdynamics.extensions;

import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricProperties;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.metrics.transformers.Transformer;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.util.TimeUtils;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import static com.appdynamics.extensions.util.ValidationUtils.isValidMetric;
import static com.appdynamics.extensions.util.ValidationUtils.isValidString;

public class MetricWriteHelper {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricWriteHelper.class);
    private ABaseMonitor baseMonitor;
    private Long startTime;
    protected ControllerInfo controllerInfo;
    //Used for Dashboard. Cache the current list of metrics.
    private boolean cacheMetrics;
    protected DerivedMetricsCalculator derivedMetricsCalculator;
    private Map<String, String> metricsMap = Maps.newConcurrentMap();

    //used from WorkBench.
    protected MetricWriteHelper() {
    }

    public MetricWriteHelper(ABaseMonitor baseMonitor) {
        AssertUtils.assertNotNull(baseMonitor, "The ABaseMonitor instance cannot be null");
        this.baseMonitor = baseMonitor;
        this.startTime = this.baseMonitor.getStartTime();
        controllerInfo = baseMonitor.getContextConfiguration().getContext().getControllerInfo();
        derivedMetricsCalculator = baseMonitor.getContextConfiguration().getContext().createDerivedMetricsCalculator();
    }

    public void printMetric(String metricPath, String metricValue, String aggregationType, String timeRollup, String clusterRollup) {
        if (isValidMetric(metricPath, metricValue, aggregationType, timeRollup, clusterRollup)) {
            if (baseMonitor.getContextConfiguration().getContext().isScheduledModeEnabled()) {
                Metric metric = new Metric(MetricPathUtils.getMetricName(metricPath), metricValue, metricPath, aggregationType, timeRollup, clusterRollup);
                logger.debug("Scheduled mode is enabled, caching the metric {}", metric);
                baseMonitor.getContextConfiguration().getContext().putInMetricCache(metricPath, metric);
            } else {
                MetricWriter metricWriter = getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
                metricWriter.printMetric(metricValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("Printing Metric [{}/{}/{}] [{}]=[{}]", aggregationType, timeRollup, clusterRollup, metricPath, metricValue);
                }
                if (cacheMetrics) {
                    Metric metric = new Metric(MetricPathUtils.getMetricName(metricPath), metricValue, metricPath, aggregationType, timeRollup, clusterRollup);
                    baseMonitor.getContextConfiguration().getContext().putInMetricCache(metricPath, metric);
                }
            }
            addForDerivedMetricsCalculation(metricPath, metricValue);
            metricsMap.put(metricPath, metricValue);
        } else {
            logger.warn("The metric is not valid. Not reporting the metric to the machine agent. Name - {}, Value - {}, Path - {}, Qualifiers - {}, {}, {}",
                    MetricPathUtils.getMetricName(metricPath), metricValue, metricPath, aggregationType, timeRollup,
                    clusterRollup);
        }
    }

    protected void addForDerivedMetricsCalculation(String metricPath, String metricValue) {
        if (derivedMetricsCalculator != null) {
            derivedMetricsCalculator.addToBaseMetricsMap(metricPath, metricValue);
        }
    }

    public void transformAndPrintMetrics(List<Metric> metrics) {
        Transformer transformer = new Transformer(metrics);
        transformer.transform();
        printMetric(metrics);
    }

    public void printMetric(List<Metric> metrics) {
        AssertUtils.assertNotNull(metrics, "The metrics cannot be null");
        for (Metric derivedMetric : metrics) {
            String metricPath = derivedMetric.getMetricPath();
            String metricValue = derivedMetric.getMetricValue();
            MetricProperties metricProperties = derivedMetric.getMetricProperties();
            String aggregationType = metricProperties.getAggregationType();
            String timeRollUpType = metricProperties.getTimeRollUpType();
            String clusterRollUpType = metricProperties.getClusterRollUpType();
            printMetric(metricPath, metricValue, aggregationType, timeRollUpType, clusterRollUpType);
        }

    }

    public void printMetric(String metricPath, BigDecimal value, String metricType) {
        if (isValidString(metricPath, metricType) && value != null) {
            String valStr = value.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            String[] qualifiers = createMetricType(metricType);
            printMetric(metricPath, valStr, qualifiers[0], qualifiers[1], qualifiers[2]);
        } else {
            logger.error("Cannot send the metric [{}], value=[{}] and metricType=[{}]", metricPath, value, metricType);
        }
    }

    public MetricWriter getMetricWriter(String metricPath, String aggregationType, String timeRollup, String clusterRollup) {
        MetricWriter writer = baseMonitor.getContextConfiguration().getContext().getFromWriterCache(metricPath);
        if (writer == null) {
            writer = baseMonitor.getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
            baseMonitor.getContextConfiguration().getContext().putInWriterCache(metricPath, writer);
        }
        return writer;
    }

    /**
     * This method is invoked once all the Tasks submitted to the TaskExecutionServiceProvider are done.
     * Any sub tasks spawned by the tasks in the TaskExecutionServiceProvider needs to be synchronized such that the
     * task completes its execution only after all its sub tasks are done.
     * Tasks done by this method:
     * -------------------------
     * 1. Triggers the DerivedMetricsCalculator based on all the metrics published from all the tasks in a job
     *    run.
     * 2. Uploads the dashboard if initialized properly and not uploaded already.
     * 3. Prints the total metrics published in the current job run.
     * 4. Logs the total execution time from the time execute() method is triggered to the time "Metrics uploaded"
     *    metric is published.
     *
     */
    public void onComplete() {
        int baseMetricsSize = 0;
        if (derivedMetricsCalculator != null) {
            triggerDerivedMetrics();
        }
        baseMonitor.getContextConfiguration().getContext().getDashboardModule().uploadDashboard();
        printMetricsUploaded();
        logTime();
    }

    private void triggerDerivedMetrics() {
        int baseMetricsSize;List<Metric> metricList = derivedMetricsCalculator.calculateAndReturnDerivedMetrics();
        baseMetricsSize = metricsMap.size();
        logger.debug("Total number of base metrics reported in this job run are : {}", baseMetricsSize);
        transformAndPrintMetrics(metricList);
        logger.debug("Total number of derived metrics reported in this job run are : {}", metricsMap.size() - baseMetricsSize);
        derivedMetricsCalculator.clearBaseMetricsMap();
    }

    private void printMetricsUploaded() {
        printMetric(baseMonitor.getContextConfiguration().getMetricPrefix()+"|"+"Metrics Uploaded",
                String.valueOf(metricsMap.size() + 1 ),"AVERAGE", "AVERAGE","COLLECTIVE" );
        logger.debug("Total number of metrics reported in this job run are : {}", metricsMap.size());
    }

    private void logTime() {
        Long endTime = System.currentTimeMillis();
        logger.info("Finished executing " + baseMonitor.getMonitorName() + " at " + TimeUtils.getFormattedTimestamp(endTime, "yyyy-MM-dd HH:mm:ss z"));
        Long totalTime = endTime - startTime;
        logger.info("Total time taken to execute " + baseMonitor.getMonitorName() + " : " + totalTime + " ms");
    }

    public boolean isCacheMetrics() {
        return cacheMetrics;
    }

    public void setCacheMetrics(boolean cacheMetrics) {
        this.cacheMetrics = cacheMetrics;
    }

    private String[] createMetricType(String metricType) {
        if (metricType != null) {
            String[] split = metricType.split("\\.");
            if (split.length == 3) {
                String clusterRollup = null;
                String timeRollup = null;
                String aggregationType = null;
                if (split[0].equals("AVG")) {
                    aggregationType = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
                } else if (split[0].equals("SUM")) {
                    aggregationType = MetricWriter.METRIC_AGGREGATION_TYPE_SUM;
                } else if (split[0].equals("OBS")) {
                    aggregationType = MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION;
                }

                if (split[1].equals("AVG")) {
                    timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;
                } else if (split[1].equals("SUM")) {
                    timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM;
                } else if (split[1].equals("CUR")) {
                    timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT;
                }

                if (split[2].equals("COL")) {
                    clusterRollup = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE;
                } else if (split[2].equals("IND")) {
                    clusterRollup = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
                }

                if (clusterRollup == null || timeRollup == null || aggregationType == null) {
                    throw new IllegalArgumentException("The Metric Type [" + metricType + "] is INVALID");
                } else {
                    return new String[]{aggregationType, timeRollup, clusterRollup};
                }
            } else {
                throw new IllegalArgumentException("The Metric Type [" + metricType + "] is INVALID");
            }
        } else {
            throw new IllegalArgumentException("The Metric Type is INVALID");
        }
    }

    public Set<String> getMetricPaths() {
        ConcurrentMap<String, Metric> map = baseMonitor.getContextConfiguration().getContext().getCachedMetrics();
        if (map != null) {
            return map.keySet();
        }
        return null;
    }


    /**
     * This is implemented only for the workbench mode.
     *
     * @param msg
     * @param e
     */
    public void registerError(String msg, Exception e) {

    }

    public void reset() {
    }
}