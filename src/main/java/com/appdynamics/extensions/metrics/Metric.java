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

import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.MetricPathUtils;

import java.util.Map;

/**
 * Created by venkata.konala on 8/31/17.
 */
public class Metric {
    private String metricName;
    private String metricValue;
    private String metricPath;
    private MetricProperties metricProperties;

    public Metric(String metricName,String metricValue,String metricPath){
        AssertUtils.assertNotNull(metricName, "Metric name cannot be null");
        AssertUtils.assertNotNull(metricValue, "Metric value cannot be null");
        AssertUtils.assertNotNull(metricPath, "Metric path cannot be null");
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricPath = metricPath;
        this.metricProperties = new DefaultMetricProperties(metricName);
    }

    public Metric(String metricName, String metricValue, String metricPath, String aggregationType,String timeRollUpType,String clusterRollUpType){
        this(metricName,metricValue,metricPath);
        this.metricProperties.setAggregationType(aggregationType);
        this.metricProperties.setTimeRollUpType(timeRollUpType);
        this.metricProperties.setClusterRollUpType(clusterRollUpType);
    }

    public Metric(String metricName, String metricValue, String metricPath, Map<String, ?> metricProperties){
        this(metricName,metricValue,metricPath);
        AssertUtils.assertNotNull(metricProperties,"Metric Properties cannot be null");
        this.metricProperties = buildMetricProperties(metricProperties);
    }

    // is this constructor required??
    public Metric(final String metricName, final String metricValue, Map<String, ?> metricProperties,
                  final MetricCharSequenceReplacer replacementMap, final String metricPrefix, final String... tokens) {
        this(metricName, metricValue,  MetricPathUtils.buildMetricPath(replacementMap, metricPrefix, tokens), metricProperties);
    }

    private MetricProperties buildMetricProperties(Map<String, ?> metricProperties){
        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(metricProperties, metricName);
        return metricPropertiesBuilder.buildMetricProperties();
    }

    public String getMetricName(){
        return metricName;
    }

    public String getMetricValue(){
        return metricValue;
    }

    public void setMetricValue(String metricValue){
        this.metricValue = metricValue;
    }

    public String getMetricPath(){
        return metricPath;
    }

    public void setMetricPath(String metricPath){
        this.metricPath = metricPath;
    }

    public MetricProperties getMetricProperties(){
        return metricProperties;
    }

    public String getAggregationType(){
        return metricProperties.getAggregationType();
    }

    public String getTimeRollUpType(){
        return metricProperties.getTimeRollUpType();
    }

    public String getClusterRollUpType(){
        return metricProperties.getClusterRollUpType();
    }

    public String toString() {
        return String.format("[%s/%s/%s] [%s]=[%s]]", metricProperties.getAggregationType(),metricProperties.getTimeRollUpType(),metricProperties.getClusterRollUpType(),getMetricPath(),getMetricValue());
    }
}
