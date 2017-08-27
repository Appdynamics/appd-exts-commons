package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.NumberUtils;
import com.google.common.base.Splitter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/9/17.
 * This class takes the baseMetricsMap (with metricNames and metricValues(BigDecimal))
 * and the expression which contains variables that are present in the baseMetricsMap.
 * The expressionEval() method calculates the values of the expression by substituting
 * values for variables, retrieved from the baseMetricsMap.
 */
public class ExpressionEvaluator {
    String metricPrefix;
    private Map<String, BigDecimal> baseMetricsMap;
    Set<String> operands;
    Set<String> modifiedOperands;
    private String expression;
    private Splitter pipeSplitter = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public ExpressionEvaluator(String metricPrefix, Map<String, BigDecimal> baseMetricsMap, Set<String> operands, Set<String> modifiedOperands,String expression){
        this.metricPrefix = metricPrefix;
        this.baseMetricsMap = baseMetricsMap;
        this.operands = operands;
        this.modifiedOperands = modifiedOperands;
        this.expression = expression;
    }

    public BigDecimal eval(){
        String modifiedExpression = getModifiedExpression(operands, modifiedOperands, expression);
        Iterator<String> modifiedOperandsIterator = modifiedOperands.iterator();
        while(modifiedOperandsIterator.hasNext()){
            String baseMetric = modifiedOperandsIterator.next();
            BigDecimal baseMetricValue = baseMetricsMap.get(metricPrefix + baseMetric);
            if(baseMetricValue != null) {
                modifiedExpression = modifiedExpression.replace(baseMetric, String.valueOf(baseMetricValue.doubleValue()));
            }
            else if(!NumberUtils.isNumber(baseMetric)){//The baseMetric is either not present in the the metricMap or its value is null
                return null;
            }
        }
        Expression e = new ExpressionBuilder(modifiedExpression).build();
        Double result = e.evaluate();
        return new BigDecimal(result);
    }

    public String getModifiedExpression(Set<String> operands, Set<String> modifiedOperands, String formula){
        for(String operand : operands){
            for(String modifiedOperand : modifiedOperands){
                if(match(operand, modifiedOperand)){
                    formula = formula.replace(operand, modifiedOperand);
                    break;
                }
            }
        }
        return formula;
    }

    public boolean match(String operand, String modifiedOperand){
        List<String> operandList = pipeSplitter.splitToList(operand);
        List<String> modifiedOperandList = pipeSplitter.splitToList(modifiedOperand);
        if(operandList.size() == modifiedOperandList.size()){
            Iterator operandListIterator = operandList.iterator();
            Iterator modifiedOperandListIterator = modifiedOperandList.iterator();
            while(operandListIterator.hasNext() && modifiedOperandListIterator.hasNext()){
                String operandSplit = operandListIterator.next().toString();
                String modifiedOperandSplit = modifiedOperandListIterator.next().toString();
                if(operandSplit.startsWith("{") && operandSplit.endsWith("}")){
                    continue;
                }
                else if(!operandSplit.equals(modifiedOperandSplit)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}

