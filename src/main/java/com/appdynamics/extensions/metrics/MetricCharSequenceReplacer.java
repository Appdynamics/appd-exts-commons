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

package com.appdynamics.extensions.metrics;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.CharMatcher;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Offers methods to perform replacements on the metric path components
 *
 * @author pradeep.nair
 */
//TODO: I don't see any unit tests for this class - there are test cases, see MetricCharSequenceReplacerTest
public class MetricCharSequenceReplacer {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricCharSequenceReplacer.class);
    //TODO: move these to a Constants class - these are very specific to this class. I dont have a separate module for this so I didn't want to make them globally available. Please suggest if you still think it should be moved out
    private static final String CONFIG_KEY = "metricReplacements";
    private static final String REPLACEMENT_KEY = "replace";
    private static final String REPLACEMENT_VALUE = "replaceWith";
    private static final String EMPTY_STRING = "";
    private final Map<String, String> replacementMap;
    private final LoadingCache<String, String> cachedReplacements;

    //TODO: this can also move to an independent enum outside of this class - discussed
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

    //TODO: From the method on line 88, this class looks like a Singleton and this constructor must be private - not a singleton, createInstance has some initialisations and checks and the map passed is not directly available from, didn't want to overload the constructor with those responsibilities
    public MetricCharSequenceReplacer(final Map<String, String> replacementMap) {
        this.replacementMap = replacementMap;
        cachedReplacements = CacheBuilder.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, String>() {
                            @Override
                            public String load(String key) {
                                return replace(key);
                            }
                        });
        logger.debug("Map and Cache initialized successfully");
    }

    // TODO: Javadoc incomplete. Line 86 - done
    /**
     * Creates a new instance of MetricCharSequenceReplacer
     *
     * @param config Map containing the replacements configuration
     * @return {@code MetricCharSequenceReplacer} returns a new instance of MetricCharSequenceReplacer
     */
    public static MetricCharSequenceReplacer createInstance(final Map<String, ?> config) {
        final List<Map<String, String>> replacements = (List<Map<String, String>>) config.get(CONFIG_KEY);
        final Map<String, String> replacementMap = new HashMap<>();
        for (Delimiter delimiter : Delimiter.values()) {
            replacementMap.put(delimiter.getDelimiter(), EMPTY_STRING);
        }
        if (replacements != null && !replacements.isEmpty()) {
            final Map<String, String> userReplacementMap = createUserReplacementMap(replacements);
            if (userReplacementMap == null || userReplacementMap.isEmpty()) {
                logger.debug("No suitable replacements configured in config.yml.");
            } else replacementMap.putAll(userReplacementMap);
        } else {
            logger.debug("No suitable replacements configured in config.yml. MetricCharReplacer will be initialized " +
                    "with default replacements, \"|,:\" will be replaced by empty string");
        }
        return new MetricCharSequenceReplacer(replacementMap);
    }

    /**
     * This methods applies configured replacements to input
     *
     * @param token Input string for which replacements have to performed
     * @return {@code String} with all the replacements
     */
    public String replace(String token) {
        String replacedToken = token;
        for (Map.Entry<String, String> replace : replacementMap.entrySet()) {
            replacedToken = replacedToken.replace(replace.getKey(), replace.getValue());
        }
        return replacedToken;
    }

    /**
     * Get replaced version of input String. The method will query the {@link LoadingCache} to fetch the output
     *
     * @param toReplace Input string for which replacements have to performed
     * @return {@code String} with all the replacements
     */
    //TODO: Provide a better name for 'in' - done
    public String getReplacementFromCache(String toReplace) {
        return cachedReplacements.getUnchecked(toReplace);
    }

    // TODO: Move static methods to a util class if required - discussed, private static method specific to this class
    private static Map<String, String> createUserReplacementMap(final List<Map<String, String>> replacements) {
        final Map<String, String> replacementMap = new HashMap<>();
        for (final Map<String, String> replacement : replacements) {
            final String replace = replacement.get(REPLACEMENT_KEY);
            final String replaceWith = replacement.get(REPLACEMENT_VALUE);
            if (replace == null || replace.isEmpty()) {
                logger.debug("Skipping entry. Value for replace cannot be null or empty string");
            } else if (replaceWith == null || hasDelimiter(replaceWith) || !CharMatcher.ascii().matchesAllOf(replaceWith)) {
                    logger.debug("replaceWith {} is null or has delimiters (|:,) or non-ascii characters. " +
                            "Defaulting replaceWith to empty string", replaceWith);
                    replacementMap.put(replace, EMPTY_STRING);
            } else {
                replacementMap.put(replace, replaceWith);
            }
        }
        return replacementMap;
    }

    private static boolean hasDelimiter(final String replaceWith) {
        for (char c : replaceWith.toCharArray()) {
            if (c == '|' || c == ':' || c == ',') return true;
        }
        return false;
    }
}
