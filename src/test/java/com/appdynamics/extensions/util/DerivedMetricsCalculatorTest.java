package com.appdynamics.extensions.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 8/14/17.
 */
public class DerivedMetricsCalculatorTest {
    private DerivedMetricsCalculator derivedMetricsCalculator;
    private List<Map<String, ?>> derivedMetricsList = Lists.newArrayList();
    private String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis";


   @Before
    public void init(){
       Map<String, Map<String, String>> nameAndProperty = Maps.newHashMap();
       Map<String, String> derivedMetricProperties = Maps.newHashMap();
       derivedMetricProperties.put("alias","ratio");
       derivedMetricProperties.put("derivedMetricPath", "{x}|CPU|{z}|ratio");
       derivedMetricProperties.put("formula", "({x}|Queue|{y}|hits + {x}|CPU|{z}|misses) / 2");
       nameAndProperty.put("ratio", derivedMetricProperties);
       derivedMetricsList.add(nameAndProperty);
       derivedMetricsCalculator = new DerivedMetricsCalculator(derivedMetricsList, metricPrefix);
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", "1");
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|misses", "2");
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|hits", "1");
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|CPU|CPU2|misses", "2");
    }

    @Test
    public void addToBaseMetricMapTest(){
        Assert.assertTrue(derivedMetricsCalculator.baseMetricsMap.size() == 4);
        Assert.assertTrue(derivedMetricsCalculator.baseMetricsMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits") == null);
    }

    @Test
    public void calculateAndReturnDerivedMetricsTest(){
        Multimap<String, MetricProperties> derivedMetricsMultiMap= derivedMetricsCalculator.calculateAndReturnDerivedMetrics();
        Assert.assertTrue(derivedMetricsMultiMap.size() == 2);
        Assert.assertTrue(derivedMetricsMultiMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|ratio").size() == 1);
        Assert.assertTrue(derivedMetricsMultiMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|ratio").iterator().next().getMetricValue().equals(new BigDecimal("1.5")));
    }

    @Test
    public void getMetricNameTest(){
        String metricName1 = derivedMetricsCalculator.getMetricName("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits");
        Assert.assertTrue(metricName1.equals("hits"));
        String metricName2 = derivedMetricsCalculator.getMetricName("");
        Assert.assertTrue(metricName2 == null);
    }

}
