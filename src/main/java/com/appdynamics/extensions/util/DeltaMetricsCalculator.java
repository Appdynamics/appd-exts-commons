package com.appdynamics.extensions.util;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class DeltaMetricsCalculator {

    private final Cache<String, BigDecimal> deltaCache;

    public DeltaMetricsCalculator(int durationInSeconds){
        this.deltaCache = CacheBuilder.newBuilder().expireAfterWrite(durationInSeconds, TimeUnit.MINUTES).build();
    }

    BigDecimal calculateDelta(String metricPath, BigDecimal currentValue){
        if(currentValue != null) {
            BigDecimal prev = (BigDecimal)this.deltaCache.getIfPresent(metricPath);
            this.deltaCache.put(metricPath, currentValue);
            if(prev != null) {
                return currentValue.subtract(prev);
            }
        }
        return null;
    }
}
