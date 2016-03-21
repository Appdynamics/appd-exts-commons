package com.appdynamics.extensions.workbench.metric;

import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.workbench.ui.MetricStoreStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by abey.tom on 3/16/16.
 */
public class WorkbenchMetricStore extends MetricWriteHelper {
    public static final Logger logger = LoggerFactory.getLogger(WorkbenchMetricStore.class);
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

    public void printMetric(String metricPath, BigDecimal value, String metricType) {
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
    }

    public Set<String> getMetricPaths() {
        return metricMap.keySet();
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

        public ErrorDetail(String message, String stackTrace) {
            this.message = message;
            this.stackTrace = stackTrace;
        }

        public String getMessage() {
            return message;
        }

        public String getStackTrace() {
            return stackTrace;
        }
    }
}
