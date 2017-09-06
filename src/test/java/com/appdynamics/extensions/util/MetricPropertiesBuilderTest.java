package com.appdynamics.extensions.util;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class MetricPropertiesBuilderTest {
    Map<String, ? super Object> metricPropertiesMap = Maps.newHashMap();
    MetricPropertiesBuilder metricPropertiesBuilder;
    @Before
    public void init(){
        metricPropertiesMap.put("alias", "ratio_alias");
        metricPropertiesMap.put("multiplier", 4);
        metricPropertiesMap.put("aggregationType", "4");
        metricPropertiesMap.put("clusterRollUpType", "COLLECTIVE");
        metricPropertiesBuilder = new MetricPropertiesBuilder(metricPropertiesMap, "ratio_original");
        metricPropertiesMap.put("delta", true);
    }

    @Test
    public void buildMetricPropertiesTest(){
        MetricProperties metricProperties = metricPropertiesBuilder.buildMetricProperties();
        Assert.assertTrue(metricProperties.getAlias().equals("ratio_alias"));
        Assert.assertTrue(metricProperties.getMultiplier().equals(new BigDecimal("4")));
        Assert.assertTrue(metricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getClusterRollUpType().equals("COLLECTIVE"));
        Assert.assertTrue(metricProperties.getDelta() == true);
    }

    @Test
    public void nullValuesTest(){
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
