package com.appdynamics.extensions.util;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by abey.tom on 3/15/16.
 */
public class MetricWriteHelper {
    public static final Logger logger = LoggerFactory.getLogger(MetricWriteHelper.class);
    private Cache<String, MetricWriter> writerCache;
    private Cache<String, Metric> metricCache;

    private AManagedMonitor managedMonitor;
    private boolean scheduledMode;
    //Used for Dashboard. Cache the current list of metrics.
    private boolean cacheMetrics;

    protected MetricWriteHelper() {
    }

    public MetricWriteHelper(AManagedMonitor managedMonitor) {
        AssertUtils.assertNotNull(managedMonitor, "The AManagedMonitor instance cannot be null");
        this.managedMonitor = managedMonitor;
        writerCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
        metricCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build();
    }

    public void printMetric(String metricPath, String metricValue, String aggregationType, String timeRollup, String clusterRollup) {
        if (!Strings.isNullOrEmpty(metricPath) && !Strings.isNullOrEmpty(metricValue)
                && !Strings.isNullOrEmpty(timeRollup) && !Strings.isNullOrEmpty(metricPath)
                && !Strings.isNullOrEmpty(clusterRollup)) {
            if (isScheduledMode()) {
                Metric metric = new Metric(metricPath, metricValue, aggregationType, timeRollup, clusterRollup);
                metricCache.put(metricPath, metric);
                logger.debug("Scheduled mode is enabled, caching the metric {}", metric);
            } else {
                MetricWriter metricWriter = getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
                metricWriter.printMetric(metricValue);
                if (logger.isDebugEnabled()) {
                    logger.debug("Printing Metric [{}/{}/{}] [{}]=[{}]", aggregationType, timeRollup, clusterRollup, metricPath, metricValue);
                }
                if(cacheMetrics){
                    Metric metric = new Metric(metricPath, metricValue, aggregationType, timeRollup, clusterRollup);
                    metricCache.put(metricPath, metric);
                }
            }
        } else {
            Metric arg = new Metric(metricPath, metricValue, aggregationType, timeRollup, clusterRollup);
            logger.error("The metric is not valid {}", arg);
        }
    }

    public void printAllFromCache() {
        ConcurrentMap<String, Metric> map = metricCache.asMap();
        Set<String> keys;
        if (map != null && (keys = map.keySet()) != null) {
            for (String key : keys) {
                Metric metric = map.get(key);
                MetricType metricType = metric.getMetricType();
                MetricWriter writer = getMetricWriter(metric.getPath(), metricType.aggregationType, metricType.timeRollup, metricType.clusterRollup);
                logger.debug("Printing Metric {}", metric);
                writer.printMetric(metric.getValue());
            }
        } else {
            logger.info("The Metric Cache is empty, no values are present");
        }
    }


    public void printMetric(String metricPath, BigDecimal value, String metricType) {
        if (!Strings.isNullOrEmpty(metricPath) && value != null && !Strings.isNullOrEmpty(metricType)) {
            String valStr = value.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            if (isScheduledMode()) {
                Metric metric = new Metric(metricPath, valStr, createMetricType(metricType));
                metricCache.put(metricPath, metric);
                logger.debug("Scheduled mode is enabled, caching the metric {}", metric);
            } else {
                MetricWriter metricWriter = getMetricWriter(metricPath, metricType);
                metricWriter.printMetric(valStr);
            }
        } else {
            logger.error("Cannot send the metric [{}], value=[{}] and metricType=[{}]", metricPath, value, metricType);
        }
    }

    private MetricWriter getMetricWriter(String metricPath, String aggregationType, String timeRollup, String clusterRollup) {
        MetricWriter writer = writerCache.getIfPresent(metricPath);
        if (writer == null) {
            writer = managedMonitor.getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
            writerCache.put(metricPath, writer);
        }
        return writer;
    }

    private MetricWriter getMetricWriter(String metricPath, String metricType) {
        MetricWriter metricWriter = writerCache.getIfPresent(metricPath);
        if (metricWriter != null) {
            return metricWriter;
        } else {
            MetricType type = createMetricType(metricType);
            return getMetricWriter(metricPath, type.aggregationType, type.timeRollup, type.clusterRollup);
        }
    }

    private MetricType createMetricType(String metricType) {
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
                    return new MetricType(aggregationType, timeRollup, clusterRollup);
                }
            } else {
                throw new IllegalArgumentException("The Metric Type [" + metricType + "] is INVALID");
            }
        } else {
            throw new IllegalArgumentException("The Metric Type [" + metricType + "] is INVALID");
        }
    }

    public Set<String> getMetricPaths() {
        if (metricCache != null) {
            return metricCache.asMap().keySet();
        }
        return null;
    }

    public void reset() {

    }

    public boolean isCacheMetrics() {
        return cacheMetrics;
    }

    public void setCacheMetrics(boolean cacheMetrics) {
        this.cacheMetrics = cacheMetrics;
    }

    public boolean isScheduledMode() {
        return scheduledMode;
    }

    public void setScheduledMode(boolean scheduledMode) {
        this.scheduledMode = scheduledMode;
    }

    /**
     * This is implemented only for the workbench mode.
     *
     * @param msg
     * @param e
     */
    public void registerError(String msg, Exception e) {

    }

    public static class Metric {
        private final String path;
        private final String value;
        private final MetricType metricType;

        public Metric(String path, String value, String aggregationType, String timeRollup, String clusterRollup) {
            this.path = path;
            this.value = value;
            this.metricType = new MetricType(aggregationType, timeRollup, clusterRollup);
        }

        public Metric(String path, String value, MetricType metricType) {
            this.metricType = metricType;
            this.path = path;
            this.value = value;
        }

        public MetricType getMetricType() {
            return metricType;
        }

        public String getPath() {
            return path;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s [%s]=[%s]", metricType.toString(), path, value);
        }
    }

    public static class MetricType {
        private final String aggregationType;
        private final String clusterRollup;
        private final String timeRollup;

        public MetricType(String aggregationType, String timeRollup, String clusterRollup) {
            this.aggregationType = aggregationType;
            this.clusterRollup = clusterRollup;
            this.timeRollup = timeRollup;
        }

        public String getAggregationType() {
            return aggregationType;
        }

        public String getClusterRollup() {
            return clusterRollup;
        }

        public String getTimeRollup() {
            return timeRollup;
        }



        @Override
        public String toString() {
            return String.format("[%s/%s/%s]", aggregationType, timeRollup, clusterRollup);
        }
    }
}
