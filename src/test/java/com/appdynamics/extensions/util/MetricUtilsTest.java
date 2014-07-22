package com.appdynamics.extensions.util;


import org.junit.Assert;
import org.junit.Test;

public class MetricUtilsTest {

    MetricUtils metricUtils = new MetricUtils();

    @Test
    public void testsToConvertValueToWholeNumber(){
        Assert.assertTrue(metricUtils.toWholeNumberString(0.34).equalsIgnoreCase("1"));
        Assert.assertTrue(metricUtils.toWholeNumberString(3.34).equalsIgnoreCase("3"));
        Assert.assertTrue(metricUtils.toWholeNumberString(0.0).equalsIgnoreCase("0"));
        Assert.assertTrue(metricUtils.toWholeNumberString(1.0).equalsIgnoreCase("1"));
        Assert.assertTrue(metricUtils.toWholeNumberString("2434343231345").equalsIgnoreCase("2434343231345"));
        Assert.assertTrue(metricUtils.toWholeNumberString(25).equalsIgnoreCase("25"));
        Assert.assertTrue(metricUtils.toWholeNumberString("Hello").equalsIgnoreCase("Hello"));
    }
}
