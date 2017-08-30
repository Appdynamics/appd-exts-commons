package com.appdynamics.extensions.util.derived;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class MetricNameFetcherTest {
    private MetricNameFetcher metricNameFetcher = new MetricNameFetcher();
    private String metricPath = "Server|Server1|Queue|Q1|hits";
    @Test
    public void getMetricNameTest(){
        String metricName = metricNameFetcher.getMetricName(metricPath);
        Assert.assertTrue(metricName.equals("hits"));
    }

    @Test
    public void getNullMetricNameTest(){
        String metricName = metricNameFetcher.getMetricName(null);
        Assert.assertTrue(metricName == null);
    }
}
