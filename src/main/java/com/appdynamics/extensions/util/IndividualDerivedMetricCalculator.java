package com.appdynamics.extensions.util;

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
    private Map<String, BigDecimal> baseMetricsMap;
    private String metricPrefix;
    private String metricName;
    private String metricPath;
    private  String formula;
    private Set<String> baseMetrics;
    private SetMultimap<String, String> globalMultiMap;
    private Splitter pipeSplitter = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();
    private Multimap<String, BigDecimal> derivedMetricMap = ArrayListMultimap.create();

    public IndividualDerivedMetricCalculator(Map<String, BigDecimal> baseMetricsMap, String metricPrefix, String metricName, String metricPath, String formula, Set<String> baseMetrics, SetMultimap<String, String> globalMultiMap){
        this.baseMetricsMap = baseMetricsMap;
        this.metricPrefix = metricPrefix;
        this.metricName = metricName;
        this.metricPath = metricPath;
        this.formula = formula;
        this.baseMetrics = baseMetrics;
        this.globalMultiMap = globalMultiMap;
    }

    public Multimap<String, BigDecimal> calculateDerivedMetric(){
        substitute(metricPath, baseMetrics, globalMultiMap);
        return derivedMetricMap;
    }

    public void substitute(String path, Set<String> operands, SetMultimap<String, String> globalMultiMap){
        String variable = checkForFirstVariable(operands);
        if(variable == null){
            ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(metricPrefix, baseMetricsMap, baseMetrics, operands, formula);
            BigDecimal value = expressionEvaluator.eval();
            String key = formKey(path);
            if(value != null){
                derivedMetricMap.put(key, value);
            }
            return;
        }
        Set<String> variableValues = globalMultiMap.get(variable);
        for(String variableValue : variableValues){
            Set<String> modifiedOperands = replaceOperands(operands, variable, variableValue);
            String modifiedPath = replacePath(path, variable, variableValue);
            substitute(modifiedPath, modifiedOperands, globalMultiMap);
        }

    }

    public String checkForFirstVariable(Set<String> operands){
        for(String operand : operands){
            List<String> metricHierarchyList = pipeSplitter.splitToList(operand);
            for(String metricHierarchy : metricHierarchyList){
                if(metricHierarchy.startsWith("{") && metricHierarchy.endsWith("}")){
                    return metricHierarchy;
                }
            }
        }
        return null;
    }

    public Set<String> replaceOperands(Set<String> operands, String variable, String variableValue){
        Set<String> modifiedOperands = Sets.newHashSet();
        for(String operand : operands){
            if(operand.contains(variable)){
                operand = operand.replace(variable, variableValue);
            }
            modifiedOperands.add(operand);
        }
        return modifiedOperands;
    }

    public String replacePath(String path, String variable, String variableValue){
        if(path.contains(variable)){
            path = path.replace(variable, variableValue);
        }
        return path;
    }

    public String formKey(String path){
        String finalMetricPath = metricPrefix + path;
        return  finalMetricPath;
    }
}
