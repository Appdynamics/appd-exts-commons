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

import com.appdynamics.extensions.metrics.MetricProperties;
import com.appdynamics.extensions.metrics.MetricPropertiesBuilder;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class MetricPropertiesBuilderTest {
    @Test
    public void buildMetricPropertiesTest(){
        Map<String, ? super Object> metricPropertiesMap = Maps.newHashMap();
        MetricPropertiesBuilder metricPropertiesBuilder;
        metricPropertiesMap.put("alias", "ratio_alias");
        metricPropertiesMap.put("multiplier", 4);
        metricPropertiesMap.put("aggregationType", "4");
        metricPropertiesMap.put("clusterRollUpType", "COLLECTIVE");
        metricPropertiesBuilder = new MetricPropertiesBuilder(metricPropertiesMap, "ratio_original");
        metricPropertiesMap.put("delta", true);
        MetricProperties metricProperties = metricPropertiesBuilder.buildMetricProperties();
        Assert.assertTrue(metricProperties.getAlias().equals("ratio_alias"));
        Assert.assertTrue(metricProperties.getMultiplier().equals(new BigDecimal("4")));
        Assert.assertTrue(metricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getClusterRollUpType().equals("COLLECTIVE"));
        Assert.assertTrue(metricProperties.getDelta() == true);
    }

    @Test
    public void nullValuesTest(){
        Map<String, ? super Object> metricPropertiesMap = Maps.newHashMap();
        MetricPropertiesBuilder metricPropertiesBuilder;
        metricPropertiesMap.put("alias", null);
        metricPropertiesMap.put("multiplier", null);
        metricPropertiesMap.put("aggregationType", null);
        metricPropertiesMap.put("timeRollUpType", null);
        metricPropertiesMap.put("clusterRollUpType", null);
        metricPropertiesMap.put("delta", null);
        metricPropertiesMap.put("convert", null);
        metricPropertiesBuilder = new MetricPropertiesBuilder(metricPropertiesMap, "nullMetric");
        MetricProperties nullMetricProperties = metricPropertiesBuilder.buildMetricProperties();
        Assert.assertTrue(nullMetricProperties.getMultiplier().equals(BigDecimal.ONE));
        Assert.assertTrue(nullMetricProperties.getAlias().equals("nullMetric"));
        Assert.assertTrue(nullMetricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(nullMetricProperties.getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(nullMetricProperties.getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(nullMetricProperties.getDelta() == false);
        Assert.assertTrue(nullMetricProperties.getConversionValues() == null);
    }

}
