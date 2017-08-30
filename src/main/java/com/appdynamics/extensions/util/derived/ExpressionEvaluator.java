package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.NumberUtils;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/9/17.
 * This class takes the baseMetricsMap (with metricNames and metricValues(BigDecimal))
 * and the expression which contains variables that are present in the baseMetricsMap.
 * The expressionEval() method calculates the values of the expression by substituting
 * values for variables, retrieved from the baseMetricsMap.
 */
public class  ExpressionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(IndividualDerivedMetricProcessor.class);
    String metricPrefix;
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    Set<String> operands;
    Set<String> modifiedOperands;
    private String expression;


    public ExpressionEvaluator(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, Set<String> operands, Set<String> modifiedOperands,String expression){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.operands = operands;
        this.modifiedOperands = modifiedOperands;
        this.expression = expression;
    }

    public BigDecimal eval(){
        OperandsProcessor operandsProcessor = new OperandsProcessor(operands);
        String modifiedExpressionWithoutValues = operandsProcessor.getModifiedExpression(modifiedOperands, expression);
        String modifiedExpressionWithValues = modifiedExpressionWithoutValues;
        Iterator<String> modifiedOperandsIterator = modifiedOperands.iterator();
        while(modifiedOperandsIterator.hasNext()){
            String baseMetric = modifiedOperandsIterator.next();
            if(NumberUtils.isNumber(baseMetric)){
                continue;
            }
            MetricNameFetcher metricNameFetcher = new MetricNameFetcher();
            String baseMetricName = metricNameFetcher.getMetricName(baseMetric);
            Map<String, BigDecimal> baseMetricMap = organisedBaseMetricsMap.get(baseMetricName);
            BigDecimal baseMetricValue = baseMetricMap.get(baseMetric);
            if(baseMetricValue != null) {
                modifiedExpressionWithValues = modifiedExpressionWithValues.replace(baseMetric, String.valueOf(baseMetricValue.doubleValue()));
            }
            else{//The baseMetric is either not present in the the metricMap or its value is null
                logger.debug("The baseMetric {} in the expression {} does not exist in the baseMetricsMap ", baseMetric, modifiedExpressionWithoutValues);
                return null;
            }
        }
        long startTime = System.currentTimeMillis();
        Expression e = new ExpressionBuilder(modifiedExpressionWithValues).build();
        Double result = e.evaluate();
        long endTime = System.currentTimeMillis();
        logger.debug("The expression {} has been evaluated as {} with a value {}", modifiedExpressionWithoutValues, modifiedExpressionWithValues, result);
        logger.debug("The time taken to calculate the formula {} as {} : {} ms", modifiedExpressionWithoutValues, modifiedExpressionWithValues, endTime - startTime);
        return new BigDecimal(result);
    }
}

