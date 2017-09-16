package com.appdynamics.extensions.metrics;


import com.singularity.ee.agent.systemagent.api.MetricWriter;



public class DefaultMetricProperties extends MetricProperties {
    public DefaultMetricProperties(String metricName){
        setAlias(metricName,metricName);
        setAggregationType(MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE);
        setDelta("false");
        setMultiplier("1");
        setClusterRollUpType(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
        setTimeRollUpType(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE);
        setConversionValues(null);
    }
}
