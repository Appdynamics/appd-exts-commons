package com.appdynamics.extensions.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/15/17.
 */
public class ExpressionEvaluatorTest {
    @org.junit.Test
    public void test(){
        Map<String, BigDecimal> metrics = new HashMap<String, BigDecimal>();
        metrics.put("hits", BigDecimal.ONE);
        metrics.put("misses", BigDecimal.ONE);
        String expressionFromConfig = "((hits / (hits + misses)) * 4) ^ 4";
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(metrics, expressionFromConfig);
        Long time1 = System.currentTimeMillis();
        System.out.println("Calculated value is : " + expressionEvaluator.expressionEval());
        //Assert.assert
        Long time2 = System.currentTimeMillis();
        System.out.println("Time to perform : " + (time2 - time1)+ " milli seconds");
    }
    @org.junit.Test
    public void getBaseMetricsTest(){
        Map<String, BigDecimal> metrics = new HashMap<String, BigDecimal>();
        metrics.put("hits", BigDecimal.ONE);
        metrics.put("misses", BigDecimal.ONE);
        String expressionFromConfig = "(hits /  (hits + misses)) * 0.5";
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(metrics, expressionFromConfig);
        Set<String> testSet = expressionEvaluator.getBaseMetricsFromExpression(expressionFromConfig);
        System.out.println("Set is :" + testSet);
    }
}
