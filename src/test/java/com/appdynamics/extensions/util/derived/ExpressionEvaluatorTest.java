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
   /* @Before
    public void init(){
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|misses", BigDecimal.ONE);
        missesMap.put("Server2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|{y}|hits");
        operands.add("{x}|misses");
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Q1|hits");
        modifiedOperands.add("Server1|misses");
        String expression = "({x}|{y}|hits / ({x}|{y}|hits + {x}|misses)) * 4";
        expressionEvaluator = new ExpressionEvaluator(organisedBaseMetricsMap, operands, modifiedOperands, expression);
    }

    @Test
    public void evalTest(){
        BigDecimal value = expressionEvaluator.eval();
        Assert.assertTrue(value.equals(new BigDecimal("2")));
     }

     @Test
    public void nullEvalValueTest(){
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|Q1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|Q2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|misses", BigDecimal.ONE);
        missesMap.put("Server2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|{y}|hits");
        operands.add("{x}|misses");
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|Q3|hits");
        modifiedOperands.add("Server1|misses");
        String expression = "({x}|{y}|hits / ({x}|{y}|hits + {x}|misses)) * 4";
        expressionEvaluator = new ExpressionEvaluator(organisedBaseMetricsMap, operands, modifiedOperands, expression);
        BigDecimal value = expressionEvaluator.eval();
        Assert.assertTrue(value == null);
    }

    @Test
    public void complexFormulaTest(){
        Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap = Maps.newHashMap();
        Map<String, BigDecimal> hitsMap = Maps.newHashMap();
        hitsMap.put("Server1|hits", BigDecimal.ONE);
        hitsMap.put("Server2|hits", BigDecimal.ONE);
        organisedBaseMetricsMap.put("hits", hitsMap);
        Map<String, BigDecimal> missesMap = Maps.newHashMap();
        missesMap.put("Server1|misses", new BigDecimal("2"));
        missesMap.put("Server2|misses", BigDecimal.ONE);
        organisedBaseMetricsMap.put("misses", missesMap);
        Set<String> operands = Sets.newHashSet();
        operands.add("{x}|hits");
        operands.add("{x}|misses");
        Set<String> modifiedOperands = Sets.newHashSet();
        modifiedOperands.add("Server1|hits");
        modifiedOperands.add("Server1|misses");
        String expression = "(({x}|hits / ({x}|hits + {x}|misses)) * 4) ^ 4";
        expressionEvaluator = new ExpressionEvaluator(organisedBaseMetricsMap, operands, modifiedOperands, expression);
        BigDecimal value = expressionEvaluator.eval();
        Assert.assertTrue(value.equals(new BigDecimal("3.160493827160493207628633172134868800640106201171875")));
        String expression2 = "((({x}|hits / ({x}|hits + {x}|misses)) * 4) ^ 4) / 3";
        expressionEvaluator = new ExpressionEvaluator(organisedBaseMetricsMap, operands, modifiedOperands, expression2);
        BigDecimal value2 = expressionEvaluator.eval();
        Assert.assertTrue(value2.equals(new BigDecimal("1.0534979423868311432244126990553922951221466064453125")));
    }*/


}
