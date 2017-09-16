package com.appdynamics.extensions.metrics;

/**
 * Created by abey.tom on 3/17/16.
 */
public class AggregatorKey {
    private final String metricPath;
    private final String metricType;

    public AggregatorKey(String metricPath, String metricType) {
        this.metricPath = metricPath;
        this.metricType = metricType;
    }

    public String getMetricPath() {
        return metricPath;
    }

    public String getMetricType() {
        return metricType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregatorKey)) return false;

        AggregatorKey that = (AggregatorKey) o;

        if (!metricPath.equals(that.metricPath)) return false;
        return metricType.equals(that.metricType);

    }

    @Override
    public int hashCode() {
        int result = metricPath.hashCode();
        result = 31 * result + metricType.hashCode();
        return result;
    }
}
