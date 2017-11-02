package com.appdynamics.extensions.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 10/27/17.
 */
public class NumberUtilsTest {
    @Test
    public void bigIntegerIsNumberTest(){
        BigDecimal num = new BigDecimal(Double.MAX_VALUE + 10);
        Assert.assertTrue(NumberUtils.isNumber(num.toString()));
    }
}
