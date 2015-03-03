package com.appdynamics.extensions.util.metrics;

import org.junit.Assert;
import org.junit.Test;


public class RegexHashMapTest {

    public static final String ESCAPEABLE_ENTRY_1 = "ColumnFamily|.*";
    public static final String ESCAPEABLE_ENTRY_2 = "Cache|KeyCache|Hits|MeanRate";

    public static final String UNESCAPEABLE_ENTRY_1 = "ColumnFamily.*";
    public static final String UNESCAPEABLE_ENTRY_2 = "Cache,KeyCache,Hits,MeanRate";

    public static final String MISSING_ENTRY = "Hello";
    public static final String MATCHING_ENTRY = "ColumnFamily|Standard|Mean Metric|Value";
    private RegexMap<MetricOverride> regexHashMap;

    private void init(boolean escapeText){
        if(escapeText) {
            regexHashMap = new RegexMap<MetricOverride>();
            MetricOverride metric1 = createMetricOverride(ESCAPEABLE_ENTRY_1);
            regexHashMap.put(ESCAPEABLE_ENTRY_1, metric1);
            MetricOverride metric2 = createMetricOverride(ESCAPEABLE_ENTRY_2);
            regexHashMap.put(ESCAPEABLE_ENTRY_2, metric2);
        }
        else{
            regexHashMap = new RegexMap<MetricOverride>(false);
            MetricOverride metric1 = createMetricOverride(UNESCAPEABLE_ENTRY_1);
            regexHashMap.put(UNESCAPEABLE_ENTRY_1, metric1);
            MetricOverride metric2 = createMetricOverride(UNESCAPEABLE_ENTRY_2);
            regexHashMap.put(UNESCAPEABLE_ENTRY_2, metric2);
        }
    }


    private MetricOverride createMetricOverride(String metricPath){
        MetricOverride metric = new MetricOverride();
        metric.setMetricKey(metricPath);
        return metric;
    }

    @Test
    public void testGetForEscapedStringMatch(){
        init(true);
        MetricOverride metric = regexHashMap.get(ESCAPEABLE_ENTRY_2);
        Assert.assertTrue(metric != null);
        Assert.assertTrue(metric.getMetricKey().equals(ESCAPEABLE_ENTRY_2));
    }

    @Test
    public void testGetForEscapedRegexMatch(){
        init(true);
        MetricOverride metric = regexHashMap.get(MATCHING_ENTRY);
        Assert.assertTrue(metric != null);
        Assert.assertTrue(metric.getMetricKey().equals(ESCAPEABLE_ENTRY_1));
    }

    @Test
    public void testGetForUnescapedStringMatch(){
        init(false);
        MetricOverride metric = regexHashMap.get(UNESCAPEABLE_ENTRY_2);
        Assert.assertTrue(metric != null);
        Assert.assertTrue(metric.getMetricKey().equals(UNESCAPEABLE_ENTRY_2));
    }

    @Test
    public void testGetForUnescapedRegexMatch(){
        init(false);
        MetricOverride metric = regexHashMap.get(MATCHING_ENTRY);
        Assert.assertTrue(metric != null);
        Assert.assertTrue(metric.getMetricKey().equals(UNESCAPEABLE_ENTRY_1));
    }

    @Test
    public void testGetForNoMatch(){
        init(true);
        MetricOverride nullMetric = regexHashMap.get(MISSING_ENTRY);
        Assert.assertTrue(nullMetric == null);
    }


}
