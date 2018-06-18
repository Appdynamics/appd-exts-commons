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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class DerivedMetricsModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(DerivedMetricsModule.class);

    public DerivedMetricsCalculator initDerivedMetricsCalculator(Map<String, ?> config, String metricPrefix) {
        List<Map<String, ?>> derivedMetricsList = (List) config.get("derivedMetrics");
        if (derivedMetricsList != null) {
            logger.info("The DerivedMetricsCalculator is initialized");
            return new DerivedMetricsCalculator(derivedMetricsList, metricPrefix);
        } else {
            logger.info("The DerivedMetricsCalculator is not initialized.");
        }
        return null;
    }
}
