/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.metrics.derived;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/24/17.
 */
public class IndividualDerivedMetricCalculatorTest {

    @Test
    public void calculateDerivedMetricWithNoLevelDifference() throws MetricNotFoundException{
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|Q1|misses", BigDecimal.ONE);
        missesMap.put("Server2|Q2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|{y}|hits");
        operands.add("{x}|{y}|misses");
        DerivedMetricsPathHandler pathHandler = new DerivedMetricsPathHandler();
        DynamicVariablesProcessor dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operands,pathHandler);
        SetMultimap<String, String> dynamicVariables = dynamicVariablesProcessor.getDynamicVariables();
        String metricPath = "{x}|{y}|ratio";
        String formula = "({x}|{y}|hits / ({x}|{y}|hits + {x}|{y}|misses)) * 4";
        OperandsHandler operand = new OperandsHandler(formula, pathHandler);
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(organisedBaseMetricsMap, dynamicVariables, metricPath, operand,pathHandler);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server1|Q1|ratio").contains(new BigDecimal("2")));
    }

    @Test
    public void calculateDerivedMetricWithLevelDifferenceOne() throws MetricNotFoundException{
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|Q1|misses", BigDecimal.ONE);
        missesMap.put("Server2|Q2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|hits");
        operands.add("{x}|{y}|misses");
        DerivedMetricsPathHandler pathHandler = new DerivedMetricsPathHandler();
        DynamicVariablesProcessor dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operands,pathHandler);
        SetMultimap<String, String> dynamicVariables = dynamicVariablesProcessor.getDynamicVariables();
        String metricPath = "{x}|{y}|ratio";
        String formula = "({x}|hits / ({x}|hits + {x}|{y}|misses)) * 4";
        OperandsHandler operand = new OperandsHandler(formula, pathHandler);
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(organisedBaseMetricsMap, dynamicVariables, metricPath, operand,pathHandler);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server1|Q1|ratio").contains(new BigDecimal("2")));
    }

    @Test
    public void calculateDerivedMetricWithDifferentVariableInSameLevel() throws MetricNotFoundException{
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|A1|misses", BigDecimal.ONE);
        missesMap.put("Server2|A2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|{y}|hits");
        operands.add("{x}|{z}|misses");
        DerivedMetricsPathHandler pathHandler = new DerivedMetricsPathHandler();
        DynamicVariablesProcessor dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operands,pathHandler);
        SetMultimap<String, String> dynamicVariables = dynamicVariablesProcessor.getDynamicVariables();
        String metricPath = "{x}|ratio";
        String formula = "({x}|{y}|hits / ({x}|{y}|hits + {x}|{z}|misses)) * 4";
        OperandsHandler operand = new OperandsHandler(formula, pathHandler);
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(organisedBaseMetricsMap, dynamicVariables, metricPath, operand,pathHandler);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server1|ratio").contains(new BigDecimal("2")));
    }

}
