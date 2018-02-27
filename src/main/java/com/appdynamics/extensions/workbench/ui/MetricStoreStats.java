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
