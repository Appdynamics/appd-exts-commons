package com.appdynamics.extensions.util.metrics;


import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricFactoryTest {

    @Test
    public void returnEmptyMetricsWhenMapIsNull(){
        MetricFactory<String> metricFactory = new MetricFactory<String>(createEmptyMetricOverrides());
        List<Metric> metrics = metricFactory.process(null);
        Assert.assertTrue(metrics.size() == 0);
    }

    @Test
    public void returnEmptyMetricsWhenOverridesIsNull(){
        MetricFactory<String> metricFactory = new MetricFactory<String>(createMetricOverridesWithNullKey());
        List<Metric> metrics = metricFactory.process(null);
        Assert.assertTrue(metrics.size() == 0);
    }


    @Test
    public void returnValidMetricsWhenValidOverridesForStringMap(){
        MetricFactory<String> metricFactory = new MetricFactory<String>(createMetricOverrides());
        List<Metric> metrics = metricFactory.process(createStringMap());
        Assert.assertTrue(metrics.size() == 1);
    }


    @Test
    public void returnValidMetricsWhenValidOverridesForObjectMap(){
        MetricFactory<Object> metricFactory = new MetricFactory<Object>(createMetricOverrides());
        List<Metric> metrics = metricFactory.process(createObjectMap());
        Assert.assertTrue(metrics.size() == 1);
        Assert.assertTrue(metrics.get(0).getMetricPath().startsWith("Hello"));
    }

    private Map<String, Object> createObjectMap() {
        Map<String,Object> metrics = new HashMap<String, Object>();
        metrics.put("This is Ration with Cache with Valid Number",34);
        metrics.put("This is Ratio with Cache with NaN","re24");
        metrics.put("Some metrics|Should be Disabled now",34);
        metrics.put("Some metric value can be | null",null);
        metrics.put(null,56);
        return metrics;
    }

    private Map<String, String> createStringMap() {
        Map<String,String> metrics = new HashMap<String, String>();
        metrics.put("This is Ration with Cache with Valid Number","34");
        metrics.put("This is Ratio with Cache with NaN","re24");
        metrics.put("Some metrics|Should be Disabled now","34");
        return metrics;
    }

    private MetricOverride[] createEmptyMetricOverrides() {
        return new MetricOverride[0];
    }

    private MetricOverride[] createMetricOverridesWithNullKey() {
        MetricOverride override = new MetricOverride();
        override.setMetricKey(null);
        return new MetricOverride[]{override};
    }


    private MetricOverride[] createMetricOverrides() {
        MetricOverride override = new MetricOverride();
        override.setMetricKey(".*Ratio.*Cache.*");
        override.setMultiplier(10);
        override.setPrefix("Hello");
        override.setPostfix("World");

        MetricOverride override1 = new MetricOverride();
        override1.setMetricKey(".*Disabled.*");
        override1.setMultiplier(10);
        override1.setPrefix("Hello");
        override1.setDisabled(true);
        override1.setPostfix("World");

        return new MetricOverride[]{override,override1};
    }
}
