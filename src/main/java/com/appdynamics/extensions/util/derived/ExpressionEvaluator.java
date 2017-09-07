package com.appdynamics.extensions.util.derived;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/9/17.
 * This class takes the baseMetricsMap (with metricNames and metricValues(BigDecimal))
 * and the expression which contains variables that are present in the baseMetricsMap.
 * The expressionEval() method calculates the values of the expression by substituting
 * values for variables, retrieved from the baseMetricsMap.
 */
public class  ExpressionEvaluator {
    private String expression;

    public ExpressionEvaluator(String expression){
        this.expression = expression;
    }

    public BigDecimal eval(){
        Expression e = new ExpressionBuilder(expression).build();
        Double result = e.evaluate();
        return new BigDecimal(result);
    }
}

