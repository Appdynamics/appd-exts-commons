package com.appdynamics.extensions.util.transformers;

import com.appdynamics.extensions.util.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 9/5/17.
 */
public class TransformerTest {
    private Metric metric1;
    private Metric metric2;
    private Metric metric3;
    List<Metric> metricList = Lists.newArrayList();

    @Test
    public void defaultMetricPropertiesTransformerTest(){
        metric1 = new Metric("ratio", "1.0", "Custom Metrics|Redis|ratio");
        metric2 = new Metric("calls", "2.0", "Custom Metrics|calls");
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        metricPopertiesMap.put("delta",true);
        metric3  = new Metric("ratio", "4.0", "Server|Redis|Custom Metric|Redis|Server1|ratio", metricPopertiesMap);
        metricList.add(metric1);
        metricList.add(metric2);
        metricList.add(metric3);

        Transformer transformer = new Transformer(metricList);
        transformer.transform();
        Assert.assertTrue(metric1.getMetricProperties().getMultiplier().equals(new BigDecimal("1")));
        Assert.assertTrue(metric2.getMetricProperties().getAlias().equals("calls"));
        Assert.assertTrue(metric2.getMetricValue().equals("2.0"));
        Assert.assertTrue(metric3.getMetricProperties().getAlias().equals("ratio_alias"));
        Assert.assertTrue(metric3.getMetricValue() == null);
    }

    @Test
    public void deltaAndMultiplerTest(){

        metric1 = new Metric("ratio", "1.0", "Custom Metrics|Redis|ratio");
        metric2 = new Metric("calls", "2.0", "Custom Metrics|calls");
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        metricPopertiesMap.put("delta",true);
        metric3  = new Metric("ratio", "5.0", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        metricList.add(metric2);
        metricList.add(metric3);

        Transformer transformer1 = new Transformer(metricList);
        transformer1.transform();
        Assert.assertTrue(metric3.getMetricValue() == null);

        metricList.remove(metric3);
        metric3 = new Metric("ratio", "7.0", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric3);

        Transformer transformer2 = new Transformer(metricList);
        transformer2.transform();
        Assert.assertTrue(metric3.getMetricValue().equals("20.0"));
    }

    @Test
    public void convertTest(){
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        Map<String, BigDecimal> convertMap = Maps.newHashMap();
        convertMap.put("ENDANGERED",BigDecimal.ONE);
        metricPopertiesMap.put("convert",convertMap);

        metric1 = new Metric("ratio", "ENDANGERED", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        Transformer transformer2 = new Transformer(metricList);
        transformer2.transform();
        Assert.assertTrue(new BigDecimal(metric1.getMetricValue()).equals(BigDecimal.ONE));

    }
}
