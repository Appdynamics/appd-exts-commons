package com.appdynamics.extensions.workbench.ui;

import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;

import java.util.Queue;

/**
 * Created by abey.tom on 3/19/16.
 */
public class MetricStoreStats {
    public long lastRefreshed;
    private int metricCount;
    private Queue<WorkbenchMetricStore.ErrorDetail> errors;

    public long getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(long lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public int getMetricCount() {
        return metricCount;
    }

    public void setMetricCount(int metricCount) {
        this.metricCount = metricCount;
    }

    public Queue<WorkbenchMetricStore.ErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(Queue<WorkbenchMetricStore.ErrorDetail> errors) {
        this.errors = errors;
    }
}
