package com.appdynamics.extensions.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class MetricPathUtilsTest {
    private String metricPath = "Server|Server1|Queue|Q1|hits";

    @Test
    public void getMetricNameTest(){
        String metricName = MetricPathUtils.getMetricName(metricPath);
        Assert.assertTrue(metricName.equals("hits"));
    }

    @Test
    public void getNullMetricNameTest(){
        String metricName = MetricPathUtils.getMetricName(null);
        Assert.assertTrue(metricName == null);
    }
}
