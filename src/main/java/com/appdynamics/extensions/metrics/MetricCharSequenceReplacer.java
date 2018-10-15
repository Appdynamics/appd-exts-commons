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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Offers methods to perform replacements on the metric path components
 *
 * @author pradeep.nair
 */
public class MetricCharSequenceReplacer {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricCharSequenceReplacer.class);
    private final Map<String, String> replacementMap;
    private final LoadingCache<String, String> cachedReplacements;

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

    /**
     * Fallback method for cache miss.
     * Could possibly use {@code org.apache.commons.lang3.StringUtils.replace}
     * @param token Input string for which replacements have to performed
     * @return  {@code String} with all the replacements
     */
    private String replace(String token) {
        String replacedToken = token;
        for (Map.Entry<String, String> replace: replacementMap.entrySet()) {
            replacedToken = replacedToken.replace(replace.getKey(), replace.getValue());
        }
        return replacedToken;
    }

    /**
     * Get replaced version of input String. The method will query the {@link LoadingCache} to fetch the output
     * @param in Input string for which replacements have to performed
     * @return {@code String} with all the replacements
     */
    public String getReplacementFor(String in) {
        return cachedReplacements.getUnchecked(in);
    }

}
