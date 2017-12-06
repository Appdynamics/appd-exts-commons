package com.appdynamics.extensions;

import com.appdynamics.extensions.customEvents.CustomEventTrigger;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricProperties;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.metrics.transformers.Transformer;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.appdynamics.extensions.util.StringUtils.isValidMetricValue;
import static com.appdynamics.extensions.util.StringUtils.validateStrings;


public class MetricWriteHelper {

    public static final Logger logger = LoggerFactory.getLogger(MetricWriteHelper.class);

    private ABaseMonitor baseMonitor;
    //Used for Dashboard. Cache the current list of metrics.
    private boolean cacheMetrics;

    protected DerivedMetricsCalculator derivedMetricsCalculator;
    protected CustomEventTrigger customEventTrigger;
    private Map<String, String> metricsMap = Maps.newConcurrentMap();

    //used from WorkBench.
    protected MetricWriteHelper() {
    }

    MetricWriteHelper(ABaseMonitor baseMonitor) {
        AssertUtils.assertNotNull(baseMonitor, "The ABaseMonitor instance cannot be null");
        this.baseMonitor = baseMonitor;
        derivedMetricsCalculator = baseMonitor.getConfiguration().createDerivedMetricsCalculator();
        customEventTrigger = baseMonitor.getConfiguration().getCustomEventTrigger();
    }

    public void printMetric(String metricPath, String metricValue, String aggregationType, String timeRollup, String clusterRollup) {
        if (validateStrings(metricPath, metricValue, timeRollup, clusterRollup) && isValidMetricValue(metricValue)) {
            if (baseMonitor.getConfiguration().isScheduledModeEnabled()) {
                Metric metric = new Metric(MetricPathUtils.getMetricName(metricPath), metricValue, metricPath, aggregationType, timeRollup, clusterRollup);
                logger.debug("Scheduled mode is enabled, caching the metric {}", metric);
                baseMonitor.getConfiguration().putInMetricCache(metricPath, metric);
            } else {
                MetricWriter metricWriter = getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
                metricWriter.printMetric(metricValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("Printing Metric [{}/{}/{}] [{}]=[{}]", aggregationType, timeRollup, clusterRollup, metricPath, metricValue);
                }
                if (cacheMetrics) {
                    Metric metric = new Metric(MetricPathUtils.getMetricName(metricPath), metricValue, metricPath, aggregationType, timeRollup, clusterRollup);
                    baseMonitor.getConfiguration().putInMetricCache(metricPath, metric);
                }
            }
            addForDerivedMetricsCalculation(metricPath, metricValue);
            metricsMap.put(metricPath, metricValue);
        } else {
            Metric arg = new Metric(MetricPathUtils.getMetricName(metricPath), metricValue, metricPath, aggregationType, timeRollup, clusterRollup);
            logger.error("The metric is not valid {}", arg);
        }
    }

    protected void addForDerivedMetricsCalculation(String metricPath, String metricValue) {
        if(derivedMetricsCalculator != null){
            derivedMetricsCalculator.addToBaseMetricsMap(metricPath,metricValue);
        }
    }

    public void transformAndPrintMetrics(List<Metric> metrics){
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
        if (validateStrings(metricPath,metricType) && value != null) {
            String valStr = value.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            String[] qualifiers = createMetricType(metricType);
            printMetric(metricPath,valStr,qualifiers[0],qualifiers[1],qualifiers[2]);
        } else {
            logger.error("Cannot send the metric [{}], value=[{}] and metricType=[{}]", metricPath, value, metricType);
        }
    }

    public MetricWriter getMetricWriter(String metricPath, String aggregationType, String timeRollup, String clusterRollup) {
        MetricWriter writer = baseMonitor.getConfiguration().getFromWriterCache(metricPath);
        if (writer == null) {
            writer = baseMonitor.getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
            baseMonitor.getConfiguration().putInWriterCache(metricPath, writer);
        }
        return writer;
    }

    public void onComplete(){
        int baseMetricsSize = 0;
        if(derivedMetricsCalculator != null){
            List<com.appdynamics.extensions.metrics.Metric> metricList = derivedMetricsCalculator.calculateAndReturnDerivedMetrics();
            baseMetricsSize = metricsMap.size();
            logger.debug("Total number of base metrics reported in this job run are : {}", baseMetricsSize);
            transformAndPrintMetrics(metricList);
            logger.debug("Total number of derived metrics reported in this job run are : {}", metricsMap.size() - baseMetricsSize);
            derivedMetricsCalculator.clearBaseMetricsMap();
        }
        if(customEventTrigger != null){
            customEventTrigger.triggerEvents(metricsMap);
        }
        logger.debug("Total number of metrics reported in this job run are : {}", metricsMap.size());
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
        ConcurrentMap<String, Metric> map = baseMonitor.configuration.getCachedMetrics();
        if(map != null){
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
