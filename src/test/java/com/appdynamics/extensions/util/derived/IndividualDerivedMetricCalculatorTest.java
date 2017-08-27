package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.derived.IndividualDerivedMetricCalculator;
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
        Assert.assertTrue(derivedMap.size() == 2);
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
        Assert.assertTrue(derivedMap.size() == 4);
        Assert.assertTrue(derivedMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|CPU|CPU1|ratio").iterator().next().equals(new BigDecimal("0.5")));
    }

    @Test
    public void calculateDerivedMetricWithDifferentVariableInSameLevel(){
        Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", new BigDecimal("1"));
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU|CPU1|misses", new BigDecimal("2"));
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|hits", new BigDecimal("1"));
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server2|CPU|CPU2|misses", new BigDecimal("2"));
        String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";
        String metricName = "ratio";
        String metricPath = "{x}|Queue|{y}|ratio";
        String formula = "({x}|Queue|{y}|hits + {x}|CPU|{z}|misses) / 2";
        Set<String> baseMetrics = Sets.newHashSet();
        baseMetrics.add("{x}|Queue|{y}|hits");
        baseMetrics.add("{x}|CPU|{z}|misses");
        SetMultimap<String, String> globalMultiMap = HashMultimap.create();
        globalMultiMap.put("{x}", "Server1");
        globalMultiMap.put("{x}","Server2");
        globalMultiMap.put("{y}", "Q1");
        globalMultiMap.put("{y}", "Q2");
        globalMultiMap.put("{z}", "CPU1");
        globalMultiMap.put("{z}", "CPU2");
        individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(baseMetricsMap, metricPrefix, metricName, metricPath, formula, baseMetrics, globalMultiMap);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|ratio").size() == 1);
        Assert.assertTrue(derivedMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Queue|Q2|ratio").size() == 1);
    }

    @Test
    public void checkForFirstVariableTest(){
        Set<String> baseMetrics = Sets.newHashSet();
        baseMetrics.add("Server1|Queue|Q1|{a}|misses|{b}");
        String firstVariable = individualDerivedMetricCalculator.checkForFirstVariable(baseMetrics);
        Assert.assertTrue(firstVariable.equals("{a}"));
    }

    @Test
    public void replaceOperandsTest(){
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|Queue|{y}|hits");
        operands.add("{x}|Queue|{y}|misses");
        Set<String> modifiedOperands = individualDerivedMetricCalculator.replaceOperands(operands, "{x}", "Server1");
        Assert.assertTrue(modifiedOperands.contains("Server1|Queue|{y}|hits"));
        Assert.assertFalse(modifiedOperands.contains("{x}|Queue|{y}|hits"));

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
