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
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by venkata.konala on 8/24/17.
 */
public class IndividualDerivedMetricProcessorTest {
    @Test
    public void processDerivedMetricWithNoLevelDifferenceTest() throws MetricNotFoundException{
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
        DerivedMetricsPathHandler derivedMetricsPathHandler = new DerivedMetricsPathHandler();
        IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedBaseMetricsMap, metricPath, formula,derivedMetricsPathHandler);
        Multimap<String, BigDecimal> derivedMetricMap;
        derivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMetricMap.size() == 2);
        Assert.assertTrue(derivedMetricMap.get("Server1|Q1|ratio").contains(new BigDecimal("2")));
    }

    @Test
    public void processDerivedMetricWithHierarchyLevelOneDifferenceTest() throws MetricNotFoundException{
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
        DerivedMetricsPathHandler derivedMetricsPathHandler = new DerivedMetricsPathHandler();
        IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedBaseMetricsMap, metricPath, formula,derivedMetricsPathHandler);
        Multimap<String, BigDecimal> derivedMap = individualDerivedMetricProcessor.processDerivedMetric();
        Assert.assertTrue(derivedMap.size() == 2);
        Assert.assertTrue(derivedMap.get("Server1|Q1|ratio").contains(new BigDecimal("2")));
    }
}
