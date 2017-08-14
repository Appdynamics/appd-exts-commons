package com.appdynamics.extensions.util;

import com.appdynamics.extensions.NumberUtils;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/9/17.
 * This class takes the metricsMap (with metricNames and metricValues(BigDecimal))
 * and the expression which contains variables that are present in the metricsMap.
 * The expressionEval() method calculates the values of the expression by substituting
 * values for variables, retrieved from the metricsMap.
 */
public class ExpressionEvaluator {
    private Map<String, BigDecimal> metricsMap;
    private String expression;

    public ExpressionEvaluator(Map<String, BigDecimal> metricsMap, String expression){
        this.metricsMap = metricsMap;
        this.expression = expression;
    }

    public BigDecimal expressionEval(){
        Set<String> baseMetricsSet = getBaseMetricsFromExpression(expression);
        Map<String,Double> baseMetricsValueMap = Maps.newHashMap();
        Iterator<String> baseMetricSetIterator = baseMetricsSet.iterator();
        while(baseMetricSetIterator.hasNext()){
            String baseMetric = baseMetricSetIterator.next();
            BigDecimal baseMetricValue = metricsMap.get(baseMetric);
            if(baseMetricValue != null) {
                baseMetricsValueMap.put(baseMetric, baseMetricValue.doubleValue());
            }
            else if(!NumberUtils.isNumber(baseMetric)){//The baseMetric is either not present in the the metricMap or its value is null
                return null;
            }
        }
        Expression e = new ExpressionBuilder(expression).variables(baseMetricsSet).build().setVariables(baseMetricsValueMap);
        Double result = e.evaluate();
        return new BigDecimal(result);
    }

    public Set<String> getBaseMetricsFromExpression(String expression){
        Set<String> baseMetricsSet = new HashSet<String>();
        Splitter splitter = Splitter.on(CharMatcher.anyOf("(+-*/%^) "))
                .trimResults()
                .omitEmptyStrings();
        List<String> baseMetricsList = splitter.splitToList(expression);
        for(String baseMetric: baseMetricsList){
            baseMetricsSet.add(baseMetric);
        }
        return baseMetricsSet;
    }
}

