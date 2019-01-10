/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

package com.appdynamics.extensions.metrics;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class DeltaMetricsCalculator {

    private final Cache<String, BigDecimal> deltaCache;

    public DeltaMetricsCalculator(int durationInSeconds){
        this.deltaCache = CacheBuilder.newBuilder().expireAfterWrite(durationInSeconds, TimeUnit.MINUTES).build();
    }

    public BigDecimal calculateDelta(String metricPath, BigDecimal currentValue){
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
