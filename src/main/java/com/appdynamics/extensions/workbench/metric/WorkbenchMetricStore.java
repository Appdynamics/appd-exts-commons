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

package com.appdynamics.extensions.workbench.metric;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.workbench.ui.MetricStoreStats;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static com.appdynamics.extensions.util.StringUtils.isValidMetricPath;
import static com.appdynamics.extensions.util.StringUtils.isValidMetricValue;
import static com.appdynamics.extensions.util.StringUtils.validateStrings;

/**
 * Created by abey.tom on 3/16/16.
 */
public class WorkbenchMetricStore extends MetricWriteHelper {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(WorkbenchMetricStore.class);
    public long lastMetricRefresh;

    private Map<String, Queue<MetricValue>> metricMap;
    private Queue<ErrorDetail> errors;
    private ResetListener listener;

    private static final WorkbenchMetricStore _instance = new WorkbenchMetricStore();

    protected WorkbenchMetricStore() {
        metricMap = new ConcurrentHashMap<String, Queue<MetricValue>>();
    }

    public static WorkbenchMetricStore getInstance() {
        return _instance;
    }

    public static void initialize(DerivedMetricsCalculator derivedMetricsCalculator) {
        _instance.derivedMetricsCalculator = derivedMetricsCalculator;
    }

    @Override
    public void printMetric(String metricPath, String metricValue, String aggregationType, String timeRollup, String clusterRollup) {
        if (validateStrings(metricPath, metricValue) && isValidMetricValue(metricValue) && isValidMetricPath(metricPath)) {
            addMetric(metricPath, new BigDecimal(metricValue));
        } else {
            logger.error("The metric is not valid. Path - [{}], Value - {}", metricPath, metricValue);
        }
    }

    public void printMetric(String metricPath, BigDecimal value, String metricType) {
        if (validateStrings(metricPath) && isValidMetricPath(metricPath)) {
            addMetric(metricPath, value);
        } else {
            logger.error("The metric is not valid. Path - [{}], Value - {}", metricPath, value);
        }
    }

    private void addMetric(String metricPath, BigDecimal value) {
        logger.debug("Adding the metric [{}] with value [{}]", metricPath, value);
        Queue<MetricValue> values = metricMap.get(metricPath);
        if (values == null) {
            synchronized (this) {
                values = metricMap.get(metricPath);
                if (values == null) {
                    values = new LinkedBlockingQueue<MetricValue>();
                    lastMetricRefresh = System.currentTimeMillis();
                    metricMap.put(metricPath, values);
                }
            }
        }
        values.add(new MetricValue(value));
        //Keep only the latest 60 values
        if (values.size() > 60) {
            values.remove();
        }
        addForDerivedMetricsCalculation(metricPath, value.toString());
    }

    public Set<String> getMetricPaths() {
        return metricMap.keySet();
    }

    public String getMetricPathsAsPlainStr() {
        Set<String> metricPaths = getMetricPaths();
        if (metricPaths != null) {
            ArrayList<String> strings = new ArrayList<String>(metricPaths);
            Collections.sort(strings);
            StringBuilder sb = new StringBuilder();
            for (String string : strings) {
                sb.append(string).append("\n");
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public List<MetricData> getMetricData(String metricPath) {
        Queue<MetricValue> values = metricMap.get(metricPath);
        if (values != null) {
            MetricValue[] vals = values.toArray(new MetricValue[values.size()]);
            return Collections.singletonList(new MetricData(metricPath, vals));
        }
        return null;
    }

    public static class MetricData {
        private String metricPath;
        MetricValue[] values;

        public MetricData(String metricPath, MetricValue[] values) {
            this.metricPath = metricPath;
            this.values = values;
        }

        public String getMetricPath() {
            return metricPath;
        }

        public void setMetricPath(String metricPath) {
            this.metricPath = metricPath;
        }

        public MetricValue[] getValues() {
            return values;
        }

        public void setValues(MetricValue[] values) {
            this.values = values;
        }
    }

    public static class MetricValue {
        private BigDecimal value;
        private long timestamp;

        public MetricValue(BigDecimal value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public BigDecimal diff(MetricValue current, int millis) {
            BigDecimal valueDiff = current.value.subtract(value);
            double timeDiff = current.timestamp - timestamp;
            if (timeDiff > 0) {
                double val = timeDiff / millis;
                if (val > 0) {
                    return valueDiff.divide(new BigDecimal(val), 0, BigDecimal.ROUND_HALF_UP);
                }
            }
            return null;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public BigDecimal getValue() {
            return value;
        }
    }

    @Override
    public void reset() {
        logger.info("Received a reset event. Clearing the metrics");
        metricMap.clear();
        lastMetricRefresh = System.currentTimeMillis();
        if (listener != null) {
            listener.onReset();
        }
    }

    public MetricStoreStats getStats() {
        MetricStoreStats stats = new MetricStoreStats();
        stats.setLastRefreshed(lastMetricRefresh);
        stats.setMetricCount(metricMap.keySet().size());
        stats.setErrors(errors);
        return stats;
    }

    public String getStatsAsStr() {
        StringBuilder sb = new StringBuilder();
        sb.append("Last Refreshed: ").append(new Date(lastMetricRefresh)).append("\n");
        sb.append("  Metric Count: ").append(metricMap.keySet().size()).append("\n");
        sb.append("   Error Count: ").append(errors != null ? errors.size() : "0").append("\n");
        return sb.toString();
    }

    @Override
    public void registerError(String msg, Exception e) {
        if (errors == null) {
            errors = new LinkedBlockingDeque<ErrorDetail>();
        }
        errors.add(new ErrorDetail(msg, toString(msg, e)));
        //There is a race condition, i dont really care
        if (errors.size() > 20) {
            errors.remove();
        }
    }

    private String toString(String msg, Exception e) {
        if (e != null) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            if (e.getMessage() != null && msg != null
                    && !msg.equals(e.getMessage())) {
                printWriter.println(e.getMessage());
            }
            e.printStackTrace(printWriter);
            printWriter.flush();
            return writer.toString();
        } else {
            return "";
        }
    }

    public void setResetListener(ResetListener listener) {
        this.listener = listener;
    }

    public interface ResetListener {
        void onReset();
    }

    public static class ErrorDetail {
        private String message;
        private String stackTrace;
        private Date date;

        public ErrorDetail(String message, String stackTrace) {
            this.message = message;
            this.stackTrace = stackTrace;
            this.date = new Date();
        }

        public String getMessage() {
            return message;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public Date getDate() {
            return date;
        }
    }
}
