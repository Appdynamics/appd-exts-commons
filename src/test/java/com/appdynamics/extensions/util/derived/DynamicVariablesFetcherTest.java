package com.appdynamics.extensions.util.derived;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class DynamicVariablesFetcherTest {
    private DynamicVariablesFetcher dynamicVariablesFetcher;

    @Before
    public void init(){
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
        dynamicVariablesFetcher = new DynamicVariablesFetcher(organisedBaseMetricsMap, operands);
    }

    @Test
    public void getDynamicVariablesTest(){
        SetMultimap<String, String> dynamicVariables = HashMultimap.create();
        dynamicVariables = dynamicVariablesFetcher.getDynamicVariables();
        Assert.assertTrue(dynamicVariables.get("{x}").size() == 2);
        Assert.assertTrue(dynamicVariables.get("{y}").size() == 2);
        Assert.assertTrue(dynamicVariables.get("{x}").contains("Server1"));
        Assert.assertTrue(dynamicVariables.get("{y}").contains("Q1"));
    }
}
