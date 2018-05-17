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

package com.appdynamics.extensions.metrics.transformers;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.DeltaMetricsCalculator;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.NumberUtils;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/31/17.
 */
class DeltaTranform {
    private static final org.slf4j.Logger logger = ExtensionsLoggerFactory.getLogger(DeltaTranform.class);
    private static DeltaMetricsCalculator deltaCalculator = new DeltaMetricsCalculator(10);

    void applyDelta(Metric metric) {
        String metricValue = metric.getMetricValue();
        if (NumberUtils.isNumber(metricValue) && metric.getMetricProperties().getDelta() == true) {
            BigDecimal deltaValue = deltaCalculator.calculateDelta(metric.getMetricPath(), new BigDecimal(metricValue));
            if (deltaValue != null) {
                metric.setMetricValue(deltaValue.toString());
            } else {
                metric.setMetricValue(null);
            }
        }
    }
}
