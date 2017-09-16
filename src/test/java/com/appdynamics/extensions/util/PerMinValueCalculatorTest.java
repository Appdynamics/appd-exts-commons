package com.appdynamics.extensions.util;

import com.appdynamics.extensions.metrics.PerMinValueCalculator;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by abey.tom on 3/15/16.
 */
public class PerMinValueCalculatorTest {

    @Test
    public void testGetPerMinuteValue() throws Exception {
        PerMinValueCalculator agg = new PerMinValueCalculator();
        agg.getPerMinuteValue("path", new BigDecimal("100"));
        Thread.sleep(3000);
        BigDecimal perMinuteValue = agg.getPerMinuteValue("path", new BigDecimal("200"));
        System.out.println(perMinuteValue);
    }

    @Test
    public void bigDecimalTest(){
        BigDecimal decimal = new BigDecimal(100D / 3);
        System.out.println(decimal);

    }
}