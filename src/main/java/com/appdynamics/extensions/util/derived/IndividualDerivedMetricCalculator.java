package com.appdynamics.extensions.util.derived;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/23/17.
 */
public class IndividualDerivedMetricCalculator {
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private Set<String> operands;
    private SetMultimap<String, String> dynamicVariables;
    private String metricPath;
    private  String formula;
    private Multimap<String, BigDecimal> derivedMetricMap = ArrayListMultimap.create();
    private OperandsFetcher operandsFetcher = new OperandsFetcher();

    public IndividualDerivedMetricCalculator(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, SetMultimap<String, String> dynamicVariables, String metricPath, String formula){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.dynamicVariables = dynamicVariables;
        this.metricPath = metricPath;
        this.formula = formula;
        this.operands = operandsFetcher.getOperandsFromFormula(formula);
    }

    public Multimap<String, BigDecimal> calculateDerivedMetric(){
        substitute(metricPath, operands, dynamicVariables);
        return derivedMetricMap;
    }

    public void substitute(String path, Set<String> localOperands, SetMultimap<String, String> dynamicvariables){
        OperandsProcessor operandsProcessor = new OperandsProcessor(localOperands);
        String variable = operandsProcessor.checkForFirstVariable();
        if(variable == null){
            ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(organisedBaseMetricsMap, operands, localOperands, formula);
            BigDecimal value = expressionEvaluator.eval();
            if(value != null){
                derivedMetricMap.put(path, value);
            }
            return;
        }
        Set<String> variableValues = dynamicvariables.get(variable);
        for(String variableValue : variableValues){
            Set<String> modifiedOperands = operandsProcessor.getModifiedOperands(variable, variableValue);
            String modifiedPath = operandsProcessor.getModifiedPath(path, variable, variableValue);
            substitute(modifiedPath, modifiedOperands, dynamicvariables);
        }

    }
}
