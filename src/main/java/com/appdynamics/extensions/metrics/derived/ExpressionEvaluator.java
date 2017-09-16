package com.appdynamics.extensions.metrics.derived;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/9/17.
 */
public class  ExpressionEvaluator {
    private String expression;

    public ExpressionEvaluator(String expression){
        this.expression = expression;
    }

    public BigDecimal eval() throws IllegalExpressionException{
        try {
            Expression e = new ExpressionBuilder(expression).build();
            Double result = e.evaluate();
            return new BigDecimal(result);
        }
        catch(Exception exception){
            throw new IllegalExpressionException("The expression " + expression + " is illegal" + exception);
        }
    }
}

