package com.appdynamics.extensions.util.metrics;


/**
 * A POJO that can be used in config files for defining metric overrides.
 */
public class MetricOverride {

    private String metricKey;
    protected String prefix;
    protected String postfix;
    protected String aggregator;
    protected String timeRollup;
    protected String clusterRollup;
    protected int multiplier;
    protected boolean disabled;


    public String getMetricKey() {
        return metricKey;
    }

    public void setMetricKey(String metricKey) {
        this.metricKey = metricKey;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }


    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getTimeRollup() {
        return timeRollup;
    }

    public void setTimeRollup(String timeRollup) {
        this.timeRollup = timeRollup;
    }

    public String getClusterRollup() {
        return clusterRollup;
    }

    public void setClusterRollup(String clusterRollup) {
        this.clusterRollup = clusterRollup;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }
}
