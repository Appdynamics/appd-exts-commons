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
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A module to read the metric character replacement configuration from config.yml.
 * The module also initialises {@link MetricCharSequenceReplacer} with the replacement configured
 *
 * @author pradeep.nair
 */
public class MetricCharSequenceReplaceModule {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricCharSequenceReplaceModule.class);
    private static final String CONFIG_KEY = "metricCharacterReplacer";
    private static final String REPLACEMENT_KEY = "replace";
    private static final String REPLACEMENT_VALUE = "replaceWith";
    private static final String EMPTY_STRING = "";

    private MetricCharSequenceReplacer replacer;
    //TODO check length, check for *
    /**
     * Contains all the delimiters for metric path
     */
    private enum Delimiter {
        PIPE("|"),
        COLON(":"),
        COMMA(",");

        private final String delimiter;

        Delimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        public String getDelimiter() {
            return this.delimiter;
        }
    }

    /**
     * Initializes {@link MetricCharSequenceReplacer} with the values configured in config.yml
     *
     * @param config {@code Map<String, String>} map view of the config.yml
     */
    public void initMetricCharSequenceReplacer(final Map<String, ?> config) {
        // replacement map will never be null now
        final List<Map<String, String>> replacements = (List<Map<String, String>>) config.get(CONFIG_KEY);
        final Map<String, String> replacementMap = new HashMap<>();
        for (Delimiter delimiter: Delimiter.values()) {
            replacementMap.put(delimiter.getDelimiter(), EMPTY_STRING);
        }
        if (replacements != null && !replacements.isEmpty()) {
            final Map<String, String> userReplacementMap = createUserReplacementMap(replacements);
            if (userReplacementMap == null || userReplacementMap.isEmpty()) {
                logger.debug("No suitable replacements configured in config.yml.");
            } else replacementMap.putAll(userReplacementMap);
        }
        replacer = new MetricCharSequenceReplacer(replacementMap);
        logger.info("MetricCharSequenceReplacer initialized successfully");
    }

    /**
     * Returns replacer instance
     * @return {@link MetricCharSequenceReplacer}
     */
    public MetricCharSequenceReplacer getMetricSequenceReplacer() {
        return replacer;
    }

    private Map<String, String> createUserReplacementMap(final List<Map<String, String>> replacements) {
        final Map<String, String> replacementMap = new HashMap<>();
        for (final Map<String, String> replacement : replacements) {
            final String replace = replacement.get(REPLACEMENT_KEY);
            final String replaceWith = replacement.get(REPLACEMENT_VALUE);
            if (replace == null || replace.isEmpty() || replaceWith == null) {
                logger.debug("Skipping entry. Value for replace cannot be null or empty string. Value for replaceWith cannot be null");
            } else {
                if (hasDelimiter(replaceWith)) {
                    logger.debug("replaceWith {} cannot have delimiter (|:,). Defaulting replaceWith to empty string",
                            replaceWith);
                    replacementMap.put(replace, EMPTY_STRING);
                } else {
                    replacementMap.put(replace, replaceWith);
                }
            }
        }
        return replacementMap;
    }

    private boolean hasDelimiter(final String replaceWith) {
        for (char c : replaceWith.toCharArray()) {
            if (c == '|' || c == ':' || c == ',') return true;
        }
        return false;
    }
}
