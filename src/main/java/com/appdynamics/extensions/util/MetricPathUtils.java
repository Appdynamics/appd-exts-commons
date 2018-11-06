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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.google.common.base.Splitter;

import java.util.List;

public class MetricPathUtils {
    private static final String DEFAULT_METRIC_SEPARATOR = "|";

    public static final Splitter PIPE_SPLITTER = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public static String getMetricName(String metricPath) {
        if (metricPath != null) {
            List<String> splitList = PIPE_SPLITTER.splitToList(metricPath);
            if (splitList.size() > 0) {
                return splitList.get(splitList.size() - 1);
            }
        }
        return null;
    }

    /**
     * Builds the metric path. If the replacer is null then the method appends the metricPrefix and metricName with
     * default separator "|". If the replacer is not null, all applicable replacements will be done on the metricName
     * and then appends the metricPrefix and metricName with default separator "|"
     * @param replacer      {@link MetricCharSequenceReplacer} instance which contains all the configured replacements
     * @param metricPrefix  metricPrefix for the metric
     * @param metricName    name of the metric
     * @return the final metric path
     */
    public static String buildMetricPath(final MetricCharSequenceReplacer replacer, final String metricPrefix, final
    String metricName) {
        final StringBuilder builder = new StringBuilder(StringUtils.trimTrailing(metricPrefix.trim(), "|"));
        if (replacer == null) {
            return builder.append(DEFAULT_METRIC_SEPARATOR).append(metricName).toString();
        }
        return builder.append(DEFAULT_METRIC_SEPARATOR).append(replacer.getReplacementFromCache(metricName)).toString();
    }

    /**
     * Builds the metric path. If the replacer is null then the method will append the metricPrefix and metricNames with
     * default separator "|". If the replacer is not null, all applicable replacements will be done on the metricName
     * and then appends the metricPrefix and metricName with default separator "|"
     * @param replacer      {@link MetricCharSequenceReplacer} instance which contains all the configured replacements
     * @param metricPrefix  metricPrefix for the metric
     * @param metricNames   individual tokens of the metric path
     * @return the final metric path
     */
    public static String buildMetricPath(final MetricCharSequenceReplacer replacer, final String metricPrefix, final
    String... metricNames) {
        String path = StringUtils.trimTrailing(metricPrefix.trim(), "|");
        for (String partialPath : metricNames) {
            path = buildMetricPath(replacer, path, partialPath);
        }
        return path;
    }
}
