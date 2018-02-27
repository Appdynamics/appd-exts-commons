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

import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by venkata.konala on 9/7/17.
 */
class AliasTransform {
    Splitter PIPE_SPLITTER = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    void applyAlias(Metric metric){
        String metricName = metric.getMetricName();
        String alias = metric.getMetricProperties().getAlias();
        String metricPath = metric.getMetricPath();
        List<String> splitList = new ArrayList<>(PIPE_SPLITTER.splitToList(metricPath));
        if (splitList.size() > 0) {
            String metricNameFromSplit = splitList.get(splitList.size() - 1);
            if(metricNameFromSplit.equals(metricName)){
                splitList.remove(splitList.size() - 1);
                splitList.add(alias);
                metric.setMetricPath(Joiner.on("|").join(splitList));
            }
        }
    }

}
