package com.appdynamics.extensions.util.derived;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class ExpressionEvaluatorTest {

    @Test
    public void basicFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1.0 + 1.0) / 2.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void singleValuesTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("1.0");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void noSpaceInFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1)/2");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test
    public void randomSpaceFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1 ) / 2");
        Assert.assertTrue(expressionEvaluator.eval().equals(BigDecimal.ONE));
    }

    @Test(expected = IllegalExpressionException.class)
    public void incorrectFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("1+1 ) / 2");
        expressionEvaluator.eval();
    }

    @Test(expected = IllegalExpressionException.class)
    public void illegalFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1 ) / 0");
        expressionEvaluator.eval() ;
    }

   @Test(expected = IllegalExpressionException.class)
    public void illegalAndIncorrectFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1+1  / 0");
       expressionEvaluator.eval();
    }

    @Test(expected = IllegalExpressionException.class)
    public void wrongNumberFormulaTest() throws IllegalExpressionException{
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator("(1m+1  / 0");
        expressionEvaluator.eval();
    }
}
