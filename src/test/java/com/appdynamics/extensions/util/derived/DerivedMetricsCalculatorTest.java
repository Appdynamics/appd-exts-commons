package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.Metric;
import com.appdynamics.extensions.util.MetricProperties;
import com.appdynamics.extensions.util.derived.DerivedMetricsCalculator;
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
       Map<String, String> derivedMetricProperties1 = Maps.newHashMap();
       derivedMetricProperties1.put("alias","ratio");
       derivedMetricProperties1.put("derivedMetricPath", "{x}|CPU|{z}|ratio");
       derivedMetricProperties1.put("formula", "({x}|Queue|{y}|hits + {x}|CPU|{z}|misses) / 2");
       derivedMetricsList.add(derivedMetricProperties1);
       derivedMetricsCalculator = new DerivedMetricsCalculator(derivedMetricsList, metricPrefix);
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", "1");
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|misses", "2");
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|hits", "1");
       derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|CPU|CPU2|misses", "2");
    }

    @Test
    public void calculateAndReturnDerivedMetricsTest(){
        Multimap<String, Metric> derivedMetricsMultiMap= derivedMetricsCalculator.calculateAndReturnDerivedMetrics();
        Assert.assertTrue(derivedMetricsMultiMap.size() == 2);
        Assert.assertTrue(derivedMetricsMultiMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|ratio").size() == 1);
        System.out.println(derivedMetricsMultiMap);
        Assert.assertTrue(derivedMetricsMultiMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|ratio").iterator().next().getMetricValue().equals("1.5"));
    }



}
