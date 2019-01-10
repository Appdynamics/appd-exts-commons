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

package com.appdynamics.extensions.metrics;


import com.singularity.ee.agent.systemagent.api.MetricWriter;



public class DefaultMetricProperties extends MetricProperties {
    public DefaultMetricProperties(String metricName){
        setAlias(metricName,metricName);
        setAggregationType(MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE);
        setDelta("false");
        setMultiplier("1");
        setClusterRollUpType(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
        setTimeRollUpType(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE);
        setConversionValues(null);
    }
}
