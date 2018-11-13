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
//TODO: There was an unused import that caused a build failure on my box and it has been removed (com.sun.xml.internal.bind.v2.TODO)
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

    // #TODO If the suggestion in the TODO for below method is implemented, there will be only one method.
    /**
     * Builds the metric path. If the replacer is null then the method appends the metricPrefix and metricName with
     * default separator "|". If the replacer is not null, all applicable replacements will be done on the metricName
     * and then appends the metricPrefix and metricName with default separator "|"
     * @param replacer      {@link MetricCharSequenceReplacer} instance which contains all the configured replacements
     * @param metricPrefix  metricPrefix for the metric
     * @param metricName    name of the metric
     * @return the final metric path
     */

    //TODO: this method should be private
    public static String buildMetricPath(final MetricCharSequenceReplacer replacer, final String metricPrefix, final
    String metricName) {
        //TODO: there's no need to initialize a new StringBuilder every time
        final StringBuilder builder = new StringBuilder(StringUtils.trimTrailing(metricPrefix.trim(), "|"));
        if (replacer == null) {
            return builder.append(DEFAULT_METRIC_SEPARATOR).append(metricName).toString();
        }
        return builder.append(DEFAULT_METRIC_SEPARATOR).append(replacer.getReplacementFromCache(metricName)).toString();
    }

    // #TODO Can you please change the name of the third argument? It should not be metricNames(token or something similiar is suggested).
    // Also update the comment accordingly.
    // #TODO Right now you are creating a new StringBuilder object for each token passed. But you can do it with a single StringBuilder
    // object for all the token passed.
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
