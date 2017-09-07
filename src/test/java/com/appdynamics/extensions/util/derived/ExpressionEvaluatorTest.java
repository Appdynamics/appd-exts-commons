package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.derived.ExpressionEvaluator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class ExpressionEvaluatorTest {
    ExpressionEvaluator expressionEvaluator;

    @Test
    public void basicFormulaTest(){
        expressionEvaluator = new ExpressionEvaluator("(1.0 + 1.0) / 2.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void singleValuesTest(){
        expressionEvaluator = new ExpressionEvaluator("1.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

}
