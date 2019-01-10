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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.google.common.base.Splitter;

import java.util.List;

public class MetricPathUtils {
    private static final String DEFAULT_METRIC_SEPARATOR = "|";
    private static MonitorContext monitorContext;

    public static void registerMetricCharSequenceReplacer(ABaseMonitor baseMonitor) {
        monitorContext = baseMonitor.getContextConfiguration().getContext();
    }

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
     * Replaces characters (including defaults) in the String that are configured under metricReplacement
     *
     * @param toReplace {@code String} for which replacement has to be performed
     * @return  {@code String} with replaced characters
     */
    public static String getReplacedString(String toReplace) {
        MetricCharSequenceReplacer replacer = monitorContext.getMetricCharSequenceReplacer();
        if (replacer != null) {
            return replacer.getReplacementFromCache(toReplace);
        }
        return toReplace;
    }

    /**
     * Builds the metric path. If the replacer is null then the method will append the metricPrefix and metricTokens with
     * default separator "|". If the replacer is not null, all applicable replacements will be done on the metricName
     * and then appends the metricPrefix and metricName with default separator "|"
     * @param metricPrefix  metricPrefix for the metric
     * @param metricTokens   individual tokens of the metric path
     * @return the final metric path
     */
    public static String buildMetricPath(final String metricPrefix, final String... metricTokens) {
        StringBuilder pathBuilder = new StringBuilder(StringUtils.trimTrailing(metricPrefix.trim(), "|"));
        for (String token : metricTokens) {
            pathBuilder.append(DEFAULT_METRIC_SEPARATOR).append(getReplacedString(token));
        }
        return pathBuilder.toString();
    }
}
