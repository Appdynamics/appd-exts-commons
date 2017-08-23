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
    private String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";

    @Before
    public void init(){
        derivedMetricsCalculator = new DerivedMetricsCalculator(derivedMetricsList, metricPrefix);
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", "1");
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|misses", "2");
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|hits", "1");
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|CPU|CPU2|misses", "2");

        Map<String, Map<String, String>> nameAndProperty = Maps.newHashMap();
        Map<String, String> derivedMetricProperties = Maps.newHashMap();
        derivedMetricProperties.put("alias","ratio");
        derivedMetricProperties.put("formula", "(hits + misses) / 2");
        nameAndProperty.put("ratio", derivedMetricProperties);
        derivedMetricsList.add(nameAndProperty);
    }

    @Test
    public void addToBaseMetricMapTest(){
        Assert.assertTrue(derivedMetricsCalculator.baseMetricsMap.size() == 4);
        Assert.assertTrue(derivedMetricsCalculator.baseMetricsMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits") == null);
    }


    @Test
    public void localMapTest(){
        List<String> expressionList = Lists.newArrayList();
        expressionList.add("{x}");
        expressionList.add("Queue");
        expressionList.add("{y}");
        expressionList.add("CPU");
        expressionList.add("hits");
        List<String> nameList = Lists.newArrayList();
        nameList.add("Server1");
        nameList.add("Queue");
        nameList.add("Q1");
        nameList.add("CPU");
        nameList.add("hits");
        Multimap<String,String> localMap = derivedMetricsCalculator.splitAndPopulateLocalMap(expressionList, nameList);
        Assert.assertTrue(localMap.size() == 2);
        System.out.println("Multimap size ========" + localMap.size());
        System.out.println("Multimap ---->" + localMap);

    }

    @Test
    public void populateGlobalMapTest(){
        derivedMetricsCalculator.populateGlobalMultiMap("{x}|Queue|{y}|hits");
        derivedMetricsCalculator.populateGlobalMultiMap("{x}|CPU|{z}|misses");
        System.out.println("Global multi map =======" + derivedMetricsCalculator.getGlobalMultiMap());

    }
}
