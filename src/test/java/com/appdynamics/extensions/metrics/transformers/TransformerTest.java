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

package com.appdynamics.extensions.metrics.transformers;

import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 9/5/17.
 */
public class TransformerTest {


    @Test
    public void defaultMetricPropertiesTransformerTest(){
        List<Metric> metricList = Lists.newArrayList();
        Metric metric1 = new Metric("ratio", "1.0", "Custom Metrics|Redis|ratio");
        Metric metric2 = new Metric("calls", "2.0", "Custom Metrics|calls");
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        metricPopertiesMap.put("delta",true);
        Metric metric3  = new Metric("ratio", "4.0", "Server|Redis|Custom Metric|Redis|Server1|ratio", metricPopertiesMap);

        metricList.add(metric1);
        metricList.add(metric2);
        metricList.add(metric3);

        Transformer transformer = new Transformer(metricList);
        transformer.transform();
        Assert.assertTrue(metric1.getMetricProperties().getMultiplier().equals(new BigDecimal("1")));
        Assert.assertTrue(metric2.getMetricProperties().getAlias().equals("calls"));
        Assert.assertTrue(metric2.getMetricValue().equals("2"));
        Assert.assertTrue(metric3.getMetricProperties().getAlias().equals("ratio_alias"));
        Assert.assertTrue(metric3.getMetricValue() == null);

    }

    @Test
    public void deltaAndMultiplerTest(){
        List<Metric> metricList = Lists.newArrayList();
        Metric metric1 = new Metric("ratio", "1.0", "Custom Metrics|Redis|ratio");
        Metric metric2 = new Metric("calls", "2.0", "Custom Metrics|calls");
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        metricPopertiesMap.put("delta",true);
        Metric metric3  = new Metric("ratio", "5.0", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        metricList.add(metric2);
        metricList.add(metric3);

        Transformer transformer1 = new Transformer(metricList);
        transformer1.transform();
        Assert.assertTrue(metric3.getMetricValue() == null);


        Metric metric4 = new Metric("ratio", "7.0", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric4);

        Transformer transformer2 = new Transformer(metricList);
        transformer2.transform();
        Assert.assertTrue(metric4.getMetricValue().equals("20"));
    }

    @Test
    public void convertTest(){
        List<Metric> metricList = Lists.newArrayList();
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        Map<String, BigDecimal> convertMap = Maps.newHashMap();
        convertMap.put("ENDANGERED",BigDecimal.ONE);
        metricPopertiesMap.put("convert",convertMap);

        Metric metric1 = new Metric("ratio", "ENDANGERED", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        Transformer transformer2 = new Transformer(metricList);
        transformer2.transform();
        Assert.assertTrue(new BigDecimal(metric1.getMetricValue()).equals(BigDecimal.TEN));
    }

    @Test
    public void aliasTest(){
        List<Metric> metricList = Lists.newArrayList();
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10");
        Metric metric1 = new Metric("ratio", "10", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        Transformer transformer = new Transformer(metricList);
        transformer.transform();
        Assert.assertFalse(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio"));
        Assert.assertTrue(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio_alias"));

    }

    @Test
    public void valueRoundUpTestWithDecimalLessThanFive(){
        List<Metric> metricList = Lists.newArrayList();
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10.0");
        Metric metric1 = new Metric("ratio", "10.549", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        Transformer transformer = new Transformer(metricList);
        transformer.transform();
        Assert.assertFalse(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio"));
        Assert.assertTrue(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio_alias"));
        Assert.assertTrue(metric1.getMetricValue().equals("105"));

    }

    @Test
    public void valueRoundUpTestWithDecimalGreaterThanFive(){
        List<Metric> metricList = Lists.newArrayList();
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10.0");
        Metric metric1 = new Metric("ratio", "10.551", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        Transformer transformer = new Transformer(metricList);
        transformer.transform();
        Assert.assertFalse(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio"));
        Assert.assertTrue(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio_alias"));
        Assert.assertTrue(metric1.getMetricValue().equals("106"));

    }

    @Test
    public void valueRoundUpTestWithDecimalEqualToFive(){
        List<Metric> metricList = Lists.newArrayList();
        Map<String, ? super Object> metricPopertiesMap = Maps.newHashMap();
        metricPopertiesMap.put("alias", "ratio_alias");
        metricPopertiesMap.put("multiplier", "10.0");
        Metric metric1 = new Metric("ratio", "10.550", "Server|Redis|Custom Metric|Redis|Server2|ratio", metricPopertiesMap);
        metricList.add(metric1);
        Transformer transformer = new Transformer(metricList);
        transformer.transform();
        Assert.assertFalse(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio"));
        Assert.assertTrue(metric1.getMetricPath().equals("Server|Redis|Custom Metric|Redis|Server2|ratio_alias"));
        Assert.assertTrue(metric1.getMetricValue().equals("106"));

    }
}
