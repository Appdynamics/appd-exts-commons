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
import com.appdynamics.extensions.metrics.Metric;

import java.util.Map;

/**
 * Created by venkata.konala on 8/31/17.
 */
class ConvertTransform {
    private static final org.slf4j.Logger logger = ExtensionsLoggerFactory.getLogger(ConvertTransform.class);

    void convert(Metric metric) {
        Map<Object, Object> convertMap = metric.getMetricProperties().getConversionValues();
        String metricValue = metric.getMetricValue();
        if (convertMap != null && !convertMap.isEmpty() && convertMap.containsKey(metricValue)) {
            metric.setMetricValue(convertMap.get(metricValue).toString());
            logger.debug("Applied conversion on {} and replaced value {} with {}", metric.getMetricPath(), metricValue, metric.getMetricValue());
        }
    }
}
