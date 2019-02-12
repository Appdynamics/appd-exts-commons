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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import org.slf4j.Logger;

import java.util.Map;

/**
 * A module to read the metric character replacement configuration from config.yml.
 * The module also initialises {@link MetricCharSequenceReplacer} with the replacement configured
 *
 * @author pradeep.nair
 */
public class MetricCharSequenceReplaceModule {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricCharSequenceReplaceModule.class);

    private MetricCharSequenceReplacer replacer;

    /**
     * Initializes {@link MetricCharSequenceReplacer} with the values configured in config.yml
     *
     * @param config {@code Map<String, String>} map view of the config.yml
     */
    public void initMetricCharSequenceReplacer(Map<String, ?> config) {
        replacer = MetricCharSequenceReplacer.createInstance(config);
        logger.info("MetricCharSequenceReplacer initialized successfully");
    }

    public MetricCharSequenceReplacer getMetricCharSequenceReplacer() {
        return replacer;
    }
}
