package com.appdynamics.extensions.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    private String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis";

    @Before
    public void init(){
        derivedMetricsCalculator = new DerivedMetricsCalculator(derivedMetricsList, metricPrefix);
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits", "1");
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server1|misses", "2");
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|hits", "1");
        derivedMetricsCalculator.addToBaseMetricsMap("Server|Component:AppLevels|Custom Metrics|Redis|Server2|misses", "2");

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
        Assert.assertTrue(derivedMetricsCalculator.baseMetricsMap.get("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits").equals(BigDecimal.ONE));
    }

    @Test
    public void retrieveMetricNameTest(){
        String metricName = derivedMetricsCalculator.retrieveMetricName("Server|Component:AppLevels|Custom Metrics|Redis|hits");
        Assert.assertTrue(metricName.equals("hits"));
    }

    @Test
    public void retrieveServerNameTest(){
        String serverName = derivedMetricsCalculator.retrieveServerName("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits");
        Assert.assertTrue(serverName.equals("Server1"));
    }

    @Test
    public void buildOrganisedBaseMetricsMapTest(){
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = derivedMetricsCalculator.buildOrganisedBaseMetricsMap();
        Assert.assertTrue(organisedBaseMetricsMap.size() == 2);
        System.out.println("Server1 base metrics map ----------------> " + organisedBaseMetricsMap.get("Server1").toString());

    }

    @Test
    public void calculateAndReturnDerivedMetricsTest(){
        Map<String, MetricProperties> derivedMetricsMap = derivedMetricsCalculator.calculateAndReturnDerivedMetrics();
        System.out.println(derivedMetricsMap.toString());
        MetricProperties server1Ratio = derivedMetricsMap.get("Server|Component:AppLevels|Custom Metrics|Redis|derived|Server1|ratio");
        System.out.println("Alias ---------> " + server1Ratio.getAlias());
        System.out.println("Metric value --------> " + server1Ratio.getMetricValue());
        //Assert.assertTrue();
    }




}
