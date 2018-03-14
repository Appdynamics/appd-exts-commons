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


import com.google.common.base.Splitter;

import java.util.List;

public class MetricPathUtils {

    public static final Splitter PIPE_SPLITTER = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public static String getMetricName(String metricPath){
        if(metricPath != null) {
            List<String> splitList = PIPE_SPLITTER.splitToList(metricPath);
            if (splitList.size() > 0) {
                return splitList.get(splitList.size() - 1);
            }
        }
        return null;
    }
}
