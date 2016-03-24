package com.appdynamics.extensions.util;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by abey.tom on 3/15/16.
 */
public class MetricWriteHelper {
    public static final Logger logger = LoggerFactory.getLogger(MetricWriteHelper.class);
    private Cache<String, MetricWriter> writerCache;

    private AManagedMonitor managedMonitor;

    protected MetricWriteHelper() {
    }

    public MetricWriteHelper(AManagedMonitor managedMonitor) {
        AssertUtils.assertNotNull(managedMonitor, "The AManagedMonitor instance cannot be null");
        this.managedMonitor = managedMonitor;
        writerCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    }

    public void printMetric(String metricPath, String metricValue, String aggregationType, String timeRollup, String clusterRollup) {
        managedMonitor.getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup)
                .printMetric(metricValue);
    }

    public void printMetric(String metricPath, BigDecimal value, String metricType) {
        if (!Strings.isNullOrEmpty(metricPath) && value != null && !Strings.isNullOrEmpty(metricType)) {
            MetricWriter metricWriter = getMetricWriter(metricPath, metricType);
            String valStr = value.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            metricWriter.printMetric(valStr);
        } else {
            logger.error("Cannot send the metric [{}], value=[{}] and metricType=[{}]", metricPath, value, metricType);
        }
    }

    private MetricWriter getMetricWriter(String metricPath, String metricType) {
        MetricWriter metricWriter = writerCache.getIfPresent(metricPath);
        if (metricWriter != null) {
            return metricWriter;
        } else {
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
                    //Metric Once created cannot be modified.
                    logger.debug("Created a metric writer for metric={} and type={}", metricPath, metricType);
                    metricWriter = managedMonitor.getMetricWriter(metricPath, aggregationType, timeRollup, clusterRollup);
                    writerCache.put(metricPath, metricWriter);
                    return metricWriter;
                }
            } else {
                throw new IllegalArgumentException("The Metric Type [" + metricType + "] is INVALID");
            }
        }
    }

    public void reset() {

    }

    /**
     * This is implemented only for the workbench mode.
     *
     * @param msg
     * @param e
     */
    public void registerError(String msg, Exception e) {

    }
}
