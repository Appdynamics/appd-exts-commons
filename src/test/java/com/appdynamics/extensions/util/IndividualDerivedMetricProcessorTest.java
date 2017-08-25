package com.appdynamics.extensions.util;

import com.google.common.collect.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 8/24/17.
 */
public class IndividualDerivedMetricProcessorTest {
    IndividualDerivedMetricProcessor individualDerivedMetricProcessor;

    @Before
    public void init(){
        Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|misses", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|misses", BigDecimal.ONE );
        String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";
        String metricName = "ratio";
        String metricPath = "{x}|Queue|{y}|ratio";
        String formula = "({x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|misses)) * 4";
        individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(baseMetricsMap, metricPrefix, metricName, metricPath, formula);
    }

    @Test
    public void processDerivedMetricWithNoLevelDifferenceTest(){
        Multimap<String, BigDecimal> derivedMetricMap;
        derivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMetricMap.size() == 2);
        Assert.assertTrue(derivedMetricMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|ratio").iterator().next().equals(new BigDecimal("2")));
    }

    @Test
    public void processDerivedMetricWithHierarchyLevelOneDifferenceTest(){
        Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|CPU|CPU1|misses", BigDecimal.ONE);
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|CPU|CPU2|misses", BigDecimal.ONE);
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|CPU|CPU1|misses", BigDecimal.ONE);
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|CPU|CPU2|misses", BigDecimal.ONE);
        String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";
        String metricName = "ratio";
        String metricPath = "{x}|Queue|{y}|CPU|ratio";
        String formula = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|CPU|{z}|misses)";
        individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(baseMetricsMap, metricPrefix, metricName, metricPath, formula);
        Multimap<String, BigDecimal> derivedMetricMap;
        derivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMetricMap.size() == 4);
        Assert.assertTrue(derivedMetricMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|CPU|ratio").size() == 2);
    }

    @Test
    public void processDerivedMetricWithDifferentVariableInSameLevelTest(){
        Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", new BigDecimal("1"));
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|misses", new BigDecimal("2"));
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|hits", new BigDecimal("1"));
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server2|CPU|CPU2|misses", new BigDecimal("2"));
        String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";
        String metricName = "ratio";
        String metricPath = "{x}|Queue|{y}|ratio";
        String formula = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|CPU|{z}|misses)";
        individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(baseMetricsMap, metricPrefix, metricName, metricPath, formula);
        Multimap<String, BigDecimal> derivedMetricMap;
        derivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMetricMap.size() == 2);
        Assert.assertTrue(derivedMetricMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|ratio").size() == 1);
        Assert.assertTrue(derivedMetricMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|ratio").size() == 1);
    }


    @Test
    public void populateGlobalMapTest(){
        SetMultimap<String, String> globalMultiMap= HashMultimap.create();
        individualDerivedMetricProcessor.populateGlobalMultiMap("{x}|Queue|{y}|hits");
        individualDerivedMetricProcessor.populateGlobalMultiMap("{x}|Queue|{y}|misses");
        globalMultiMap = individualDerivedMetricProcessor.getGlobalMultiMap();
        Assert.assertTrue(globalMultiMap.size() == 3);
        Assert.assertTrue(globalMultiMap.get("{x}").iterator().next().equals("Server1"));
    }

    @Test
    public void splitAndPopulateLocalMapTest(){
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
        Multimap<String,String> localMap = individualDerivedMetricProcessor.splitAndPopulateLocalMap(expressionList, nameList);
        Assert.assertTrue(localMap.size() == 2);
        Assert.assertTrue(localMap.get("{x}").size() == 1);
        Assert.assertTrue(localMap.get("{y}").iterator().next().equals("Q1"));
    }
}
