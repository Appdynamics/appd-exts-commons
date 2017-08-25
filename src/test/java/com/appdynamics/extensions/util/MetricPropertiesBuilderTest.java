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
        metricPropertiesMap.put("clusterRollUp", "COLLECTIVE");
        metricPropertiesBuilder = new MetricPropertiesBuilder(metricPropertiesMap, "ratio_original", "1");
        metricPropertiesMap.put("delta", true);
        metricPropertiesMap.put("aggregateAtCluster", "true");
    }

    @Test
    public void buildMetricPropertiesTest(){
        MetricProperties metricProperties = metricPropertiesBuilder.buildMetricProperties();
        Assert.assertTrue(metricProperties.getAlias().equals("ratio_alias"));
        Assert.assertTrue(metricProperties.getMultiplier().equals(new BigDecimal("4")));
        Assert.assertTrue(metricProperties.getMetricValue().equals(BigDecimal.ONE));
        Assert.assertTrue(metricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metricProperties.getClusterRollUp().equals("COLLECTIVE"));
        Assert.assertTrue(metricProperties.getDelta() == true);
        Assert.assertTrue(metricProperties.getAggregateAtCluster() == true);
    }

    @Test
    public void nullValuesTest(){
        metricPropertiesMap.put("alias", null);
        metricPropertiesMap.put("multiplier", null);
        metricPropertiesMap.put("aggregationType", null);
        metricPropertiesMap.put("timeRollUp", null);
        metricPropertiesMap.put("clusterRollUp", null);
        metricPropertiesMap.put("delta", null);
        metricPropertiesMap.put("cluster", null);
        metricPropertiesMap.put("aggregateAtCluster", null);
        metricPropertiesBuilder = new MetricPropertiesBuilder(metricPropertiesMap, "nullMetric",null);
        MetricProperties nullMetricProperties = metricPropertiesBuilder.buildMetricProperties();
        Assert.assertTrue(nullMetricProperties.getMetricValue() == null);
        Assert.assertTrue(nullMetricProperties.getMultiplier().equals(BigDecimal.ONE));
        Assert.assertTrue(nullMetricProperties.getAlias().equals("nullMetric"));
        Assert.assertTrue(nullMetricProperties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(nullMetricProperties.getClusterRollUp().equals("INDIVIDUAL"));
        Assert.assertTrue(nullMetricProperties.getTimeRollUp().equals("AVERAGE"));
        Assert.assertTrue(nullMetricProperties.getDelta() == false);
        Assert.assertTrue(nullMetricProperties.getConversionValues() == null);
        Assert.assertTrue(nullMetricProperties.getAggregateAtCluster() == false);
    }

}
