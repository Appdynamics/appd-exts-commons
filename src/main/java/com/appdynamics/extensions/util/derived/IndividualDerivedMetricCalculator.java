package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.NumberUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Created by venkata.konala on 8/23/17.
 */
class IndividualDerivedMetricCalculator {

    private static final  Logger logger = LoggerFactory.getLogger(IndividualDerivedMetricCalculator.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private OperandsHandler operandsHandler;
    private String metricPath;
    private SetMultimap<String, String> dynamicVariables;
    private DerivedMetricsPathHandler pathHandler;
    private Multimap<String, BigDecimal> derivedMetricMap = ArrayListMultimap.create();

    IndividualDerivedMetricCalculator(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, SetMultimap<String, String> dynamicVariables, String metricPath, OperandsHandler operandsHandler,DerivedMetricsPathHandler pathHandler){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.dynamicVariables = dynamicVariables;
        this.metricPath = metricPath;
        this.operandsHandler = operandsHandler;
        this.pathHandler = pathHandler;
    }

     Multimap<String, BigDecimal> calculateDerivedMetric(){
        substitute(metricPath, operandsHandler.getBaseOperands(), dynamicVariables);
        return derivedMetricMap;
     }

     void substitute(String path, Set<String> localOperands, SetMultimap<String, String> dynamicvariables){
        String variable = operandsHandler.checkForFirstVariable(localOperands);
        if(variable == null){
            String substitutedFormula =  getValueSubstitutedFormula(localOperands);
            if(substitutedFormula != null) {
                long startTime = System.currentTimeMillis();
                ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(substitutedFormula);
                BigDecimal value = expressionEvaluator.eval();
                long endTime = System.currentTimeMillis();
                logger.debug("The time taken to calculate the formula {} : {} ms", substitutedFormula, endTime - startTime);
                if (value != null) {
                    derivedMetricMap.put(path, value);
                }
            }
            return;
        }
        Set<String> variableValues = dynamicvariables.get(variable);
        for(String variableValue : variableValues){
            Set<String> modifiedOperands = operandsHandler.getSubstitutedOperands(localOperands, variable, variableValue);
            String modifiedPath = pathHandler.getSubstitutedPath(path, variable, variableValue);
            substitute(modifiedPath, modifiedOperands, dynamicvariables);
        }

     }

    private String getValueSubstitutedFormula(Set<String> modifiedOperands){
        String modifiedExpressionWithoutValues = operandsHandler.getSubstitutedExpression(modifiedOperands);
        String modifiedExpressionWithValues = modifiedExpressionWithoutValues;
        Iterator<String> modifiedOperandsIterator = modifiedOperands.iterator();
        while(modifiedOperandsIterator.hasNext()){
            String baseMetric = modifiedOperandsIterator.next();
            if(NumberUtils.isNumber(baseMetric)){
                continue;
            }
            String baseMetricName = pathHandler.getMetricName(baseMetric);
            Map<String, BigDecimal> baseMetricMap = organisedBaseMetricsMap.get(baseMetricName);
            BigDecimal baseMetricValue = baseMetricMap.get(baseMetric);
            if(baseMetricValue != null) {
                modifiedExpressionWithValues = modifiedExpressionWithValues.replace(baseMetric, String.valueOf(baseMetricValue.doubleValue()));
            }
            else{//The baseMetric is either not present in the the metricMap or its value is null
                logger.debug("The baseMetric {} in the expression {} does not exist in the baseMetricsMap", baseMetric, modifiedExpressionWithoutValues);
                return null;
            }
        }
        logger.debug("The expression {} has been evaluated as {}", modifiedExpressionWithoutValues, modifiedExpressionWithValues);
        return modifiedExpressionWithValues;
    }
}
