package com.appdynamics.extensions.util.derived;

import com.google.common.collect.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/24/17.
 */
public class IndividualDerivedMetricProcessorTest {
    IndividualDerivedMetricProcessor individualDerivedMetricProcessor;

   @Before
    public void init(){
       Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
       Map<String, BigDecimal> hitsMap = Maps.newHashMap();
       hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
       hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
       organisedBaseMetricsMap.put("hits", hitsMap);
       Map<String, BigDecimal> missesMap = Maps.newHashMap();
       missesMap.put("Server1|Q1|misses", BigDecimal.ONE);
       missesMap.put("Server2|Q2|misses", BigDecimal.ONE);
       organisedBaseMetricsMap.put("misses", missesMap);

       String metricPath = "{x}|{y}|ratio";
       String formula = "({x}|{y}|hits / ({x}|{y}|hits + {x}|{y}|misses)) * 4";

       individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedBaseMetricsMap, metricPath, formula);
    }

    @Test
    public void processDerivedMetricWithNoLevelDifferenceTest(){
        Multimap<String, BigDecimal> derivedMetricMap;
        derivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMetricMap.size() == 2);
        Assert.assertTrue(derivedMetricMap.get("Server1|Q1|ratio").contains(new BigDecimal("2")));
    }

    @Test
    public void processDerivedMetricWithHierarchyLevelOneDifferenceTest(){
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|Q1|misses", BigDecimal.ONE);
        missesMap.put("Server2|Q2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        String metricPath = "{x}|{y}|ratio";
        String formula = "({x}|hits / ({x}|hits + {x}|{y}|misses)) * 4";
        individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedBaseMetricsMap, metricPath, formula);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server1|Q1|ratio").contains(new BigDecimal("2")));
    }

    /*@Test
    public void processDerivedMetricWithDifferentVariableInSameLevelTest(){
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|A1|misses", BigDecimal.ONE);
        missesMap.put("Server2|A2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);

        String metricPath = "{x}|ratio";
        String formula = "({x}|{y}|hits / ({x}|{y}|hits + {x}|{z}|misses)) * 4";
        individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedBaseMetricsMap, metricPath, formula);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server1|ratio").contains(new BigDecimal("2")));
    }*/
}
