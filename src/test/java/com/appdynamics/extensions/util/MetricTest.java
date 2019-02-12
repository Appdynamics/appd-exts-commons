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

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricProperties;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by venkata.konala on 9/5/17.
 */
public class MetricTest {

    @Test
    public void metricTestWithDefaultMetricProperties(){
        Metric metric = new Metric("ratio", "1.0", "Server|Redis|Custom Metric| Redis|Server1|ratio" );
        Assert.assertTrue(metric.getMetricName().equals("ratio"));
        Assert.assertTrue(metric.getMetricValue().equals("1.0"));
        Assert.assertTrue(metric.getMetricPath().equals("Server|Redis|Custom Metric| Redis|Server1|ratio"));
        MetricProperties metricProperties = metric.getMetricProperties();
        Assert.assertTrue(metricProperties.getDelta() == false);
        Assert.assertTrue(metricProperties.getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metricProperties.getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getConversionValues() == null);
        Assert.assertTrue(metricProperties.getAlias().equals("ratio"));
        Assert.assertTrue(metricProperties.getMultiplier().equals(BigDecimal.ONE));
    }

    @Test
    public void metricTestWithMetricProperties(){
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "2");
        Metric metric = new Metric("ratio", "1.0", "Server|Redis|Custom Metric| Redis|Server1|ratio", metricPopertiesMap);
        Assert.assertTrue(metric.getMetricName().equals("ratio"));
        Assert.assertTrue(metric.getMetricValue().equals("1.0"));
        Assert.assertTrue(metric.getMetricPath().equals("Server|Redis|Custom Metric| Redis|Server1|ratio"));
        MetricProperties metricProperties = metric.getMetricProperties();
        Assert.assertTrue(metricProperties.getDelta() == false);
        Assert.assertTrue(metricProperties.getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metricProperties.getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getConversionValues() == null);
        Assert.assertTrue(metricProperties.getAlias().equals("ratio_alias"));
        Assert.assertTrue(metricProperties.getMultiplier().equals(new BigDecimal("2")));
    }
}
