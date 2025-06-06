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
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class DynamicVariablesProcessorTest {
    @Test
    public void getDynamicVariablesTest() throws MetricNotFoundException{
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|misses", BigDecimal.ONE);
        missesMap.put("Server2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|{y}|hits");
        operands.add("{x}|misses");
        DynamicVariablesProcessor  dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operands,new DerivedMetricsPathHandler());
        SetMultimap<String, String> dynamicVariables = dynamicVariablesProcessor.getDynamicVariables();
        Assert.assertTrue(dynamicVariables.get("{x}").size() == 2);
        Assert.assertTrue(dynamicVariables.get("{y}").size() == 2);
        Assert.assertTrue(dynamicVariables.get("{x}").contains("Server1"));
        Assert.assertTrue(dynamicVariables.get("{y}").contains("Q1"));
    }

    @Test(expected = MetricNotFoundException.class)
    public void MetricNotFoundExceptionTest() throws MetricNotFoundException{
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|misses", BigDecimal.ONE);
        missesMap.put("Server2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|{y}|hits");
        operands.add("{x}|misses");
        operands.add("{x}|calls");
        DynamicVariablesProcessor dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operands,new DerivedMetricsPathHandler());
        SetMultimap<String, String> dynamicVariables = dynamicVariablesProcessor.getDynamicVariables();
    }
}
