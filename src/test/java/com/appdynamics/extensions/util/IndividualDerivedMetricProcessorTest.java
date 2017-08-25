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
    public void processDerivedMetricTest(){
        Multimap<String, BigDecimal> derivedMetricMap;
        derivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
        System.out.println("DerivedMetricMap1 =======" + derivedMetricMap);
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
        System.out.println("DerivedMetricMap2 =======" + derivedMetricMap);
    }

    @Test
    public void populateGlobalMapTest(){
        SetMultimap<String, String> globalMultiMap= HashMultimap.create();
        individualDerivedMetricProcessor.populateGlobalMultiMap("{x}|Queue|{y}|hits");
        individualDerivedMetricProcessor.populateGlobalMultiMap("{x}|Queue|{y}|misses");
        //System.out.println("Global multi map =======" + individualDerivedMetricProcessor.getGlobalMultiMap());
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
        //System.out.println("Multimap size ========" + localMap.size());
        //System.out.println("Multimap ---->" + localMap);
    }
}
