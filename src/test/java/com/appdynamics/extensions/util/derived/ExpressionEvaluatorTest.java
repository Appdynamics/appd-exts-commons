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
    @Before
    public void init(){
        String metricPrefix = "Server|Component:AppLevels|Custom Metrics|Redis|";
        Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q1|misses", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|hits", BigDecimal.ONE );
        baseMetricsMap.put("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|misses", BigDecimal.ONE );
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|Queue|{y}|hits");
        operands.add("{x}|Queue|{y}|misses");
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Queue|Q1|hits");
        modifiedOperands.add("Server1|Queue|Q1|misses");
        String expression = "({x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|misses)) * 4";
        expressionEvaluator = new ExpressionEvaluator(metricPrefix, baseMetricsMap, operands, modifiedOperands, expression);
    }

    @Test
    public void evalTest(){
        BigDecimal value = expressionEvaluator.eval();
        Assert.assertTrue(value.equals(new BigDecimal("2")));
     }

    @Test
    public void getMoidifiedExpressionTest(){
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|Queue|{y}|hits");
        operands.add("{x}|Queue|{y}|misses");
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Queue|Q1|hits");
        modifiedOperands.add("Server1|Queue|Q1|misses");
        String expression = "{x}|Queue|{y}|hits / ({x}|Queue|{y}|hits + {x}|Queue|{y}|misses)";
        String modifiedExpression = expressionEvaluator.getModifiedExpression(operands,modifiedOperands, expression);
        Assert.assertTrue(modifiedExpression.equals("Server1|Queue|Q1|hits / (Server1|Queue|Q1|hits + Server1|Queue|Q1|misses)"));
    }

    @Test
    public void matchTest(){
        boolean value1 = expressionEvaluator.match("{x}|Queue|{y}|hits", "Server1|Queue|Q2|hits");
        Assert.assertTrue(value1);
        boolean value2 = expressionEvaluator.match("{x}|Queue|{y}|hits", "Server1|Queue|Q2|misses");
        Assert.assertFalse(value2);
    }
}
