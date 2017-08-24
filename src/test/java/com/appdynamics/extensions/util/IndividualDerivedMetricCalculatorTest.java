package com.appdynamics.extensions.util;

import com.google.common.collect.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/24/17.
 */
public class IndividualDerivedMetricCalculatorTest {
    IndividualDerivedMetricCalculator individualDerivedMetricCalculator;

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
        Set<String> baseMetrics = Sets.newHashSet();
        baseMetrics.add("{x}|Queue|{y}|hits");
        baseMetrics.add("{x}|Queue|{y}|misses");
        SetMultimap<String, String> globalMultiMap = HashMultimap.create();
        globalMultiMap.put("{x}", "Server1");
        globalMultiMap.put("{y}", "Q1");
        globalMultiMap.put("{y}", "Q2");

        individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(baseMetricsMap, metricPrefix, metricName, metricPath, formula, baseMetrics, globalMultiMap);
    }

    @Test
    public void calculateDerivedMetricWithNoLevelDifference(){
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        System.out.println("Derived metric map ======= " + derivedMap);
    }

    @Test
    public void calculateDerivedMetricWithLevelDifferenceOne(){
        Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|CPU|CPU1|misses", BigDecimal.ONE);
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|CPU|CPU2|misses", BigDecimal.ONE);
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|CPU|CPU1|misses", BigDecimal.ONE);
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|CPU|CPU2|misses", BigDecimal.ONE);
        String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";
        String metricName = "ratio";
        String metricPath = "{x}|Queue|{y}|CPU|{z}|ratio";
        String formula = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|CPU|{z}|misses)";
        Set<String> baseMetrics = Sets.newHashSet();
        baseMetrics.add("{x}|Queue|{y}|hits");
        baseMetrics.add("{x}|Queue|{y}|CPU|{z}|misses");
        SetMultimap<String, String> globalMultiMap = HashMultimap.create();
        globalMultiMap.put("{x}", "Server1");
        globalMultiMap.put("{y}", "Q1");
        globalMultiMap.put("{y}", "Q2");
        globalMultiMap.put("{z}", "CPU1");
        globalMultiMap.put("{z}", "CPU2");
        individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(baseMetricsMap, metricPrefix, metricName, metricPath, formula, baseMetrics, globalMultiMap);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        System.out.println("Derived metric with hierarchy level difference 1 ======= " + derivedMap);

    }

    @Test
    public void checkForFirstVariableTest(){
        Set<String> baseMetrics = Sets.newHashSet();
        baseMetrics.add("Server1|Queue|Q1|{a}|misses");
        baseMetrics.add("Server1|Queue|Q1|{z}|sitsess");
        String firstVariable = individualDerivedMetricCalculator.checkForFirstVariable(baseMetrics);
        System.out.println("First Variable =========" + firstVariable);
    }

    @Test
    public void replaceOperandsTest(){
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|Queue|{y}|hits");
        operands.add("{x}|Queue|{y}|misses");
        Set<String> modifiedOperands = individualDerivedMetricCalculator.replaceOperands(operands, "{x}", "Server1");
        System.out.println("Modified Operands ======== " + modifiedOperands);

    }

    @Test
    public void replacePathTest(){
        String path = individualDerivedMetricCalculator.replacePath("{x}|Queue|{y}|hits","{x}", "Server1");
        Assert.assertTrue(path.equals("Server1|Queue|{y}|hits"));
    }

    @Test
    public void formKeyTest(){
        String key = individualDerivedMetricCalculator.formKey("Server1|Queue|Q2|misses");
        Assert.assertTrue(key.equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|misses"));
    }

}
