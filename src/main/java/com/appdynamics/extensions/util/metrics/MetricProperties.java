package com.appdynamics.extensions.util.metrics;


public class MetricProperties {

    public static final String EMPTY_STRING = "";
    public static final String METRIC_AGGREGATION_TYPE_AVERAGE = "AVERAGE";
    public static final String METRIC_TIME_ROLLUP_TYPE_AVERAGE = "AVERAGE";
    public static final String METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final double DEFAULT_MULTIPLIER = 1d;

    protected String metricPrefix;
    protected String metricPostfix;
    protected String aggregator;
    protected String timeRollup;
    protected String clusterRollup;
    protected double multiplier;
    protected boolean disabled;

    public String getMetricPrefix() {
        if(metricPrefix == null){
            return EMPTY_STRING;
        }
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public String getMetricPostfix() {
        if(metricPostfix == null){
            return EMPTY_STRING;
        }
        return metricPostfix;
    }

    public void setMetricPostfix(String metricPostfix) {
        this.metricPostfix = metricPostfix;
    }

    public String getAggregator() {
        if(aggregator == null){
            return METRIC_AGGREGATION_TYPE_AVERAGE;
        }
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getTimeRollup() {
        if(timeRollup == null){
            return METRIC_TIME_ROLLUP_TYPE_AVERAGE;
        }
        return timeRollup;
    }

    public void setTimeRollup(String timeRollup) {
        this.timeRollup = timeRollup;
    }

    public String getClusterRollup() {
        if(clusterRollup == null){
            return METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        }
        return clusterRollup;
    }

    public void setClusterRollup(String clusterRollup) {
        this.clusterRollup = clusterRollup;
    }

    public double getMultiplier() {
        if(multiplier <=0){
            return DEFAULT_MULTIPLIER;
        }
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
