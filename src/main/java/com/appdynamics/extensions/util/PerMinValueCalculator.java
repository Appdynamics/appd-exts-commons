package com.appdynamics.extensions.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by abey.tom on 3/14/16.
 */
public class PerMinValueCalculator {

    private final Cache<String, MetricValue> perMinCache;

    public PerMinValueCalculator() {
        perMinCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    }

    public BigDecimal getPerMinuteValue(String metricPath, BigDecimal currentValue) {
        if (currentValue != null) {
            MetricValue prev = perMinCache.getIfPresent(metricPath);
            MetricValue current = new MetricValue(currentValue);
            perMinCache.put(metricPath, current);
            if (prev != null) {
                return prev.diff(current, 60000);
            }
        }
        return null;
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
            if(timeDiff > 0){
                double val = timeDiff / millis;
                if(val > 0){
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
}
