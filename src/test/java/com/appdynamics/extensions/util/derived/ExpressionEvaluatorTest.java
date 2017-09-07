package com.appdynamics.extensions.util.derived;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class ExpressionEvaluatorTest {

    @Test
    public void basicFormulaTest(){
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1.0 + 1.0) / 2.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void singleValuesTest(){
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("1.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

}
