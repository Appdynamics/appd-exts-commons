package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        List<Metric> derivedMetricList= derivedMetricsCalculator.calculateAndReturnDerivedMetrics();
        Assert.assertTrue(derivedMetricList.size() == 2);
        for(Metric metric : derivedMetricList){
            if(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|ratio")){
                Assert.assertTrue(metric.getMetricValue().equals("1.5"));
            }
        }
    }



}
