package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.NumberUtils;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/28/17.
 */
public class DynamicVariablesFetcher {
    private static final Logger logger = LoggerFactory.getLogger(DynamicVariablesFetcher.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private Set<String> operands;
    private Splitters splitters = new Splitters();

    public DynamicVariablesFetcher(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, Set<String> operands){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.operands = operands;
    }

    public SetMultimap<String, String> getDynamicVariables(){
        SetMultimap<String, String> dynamicVariables = HashMultimap.create();
        for(String operand : operands){
            if(!NumberUtils.isNumber(operand)) {
                SetMultimap<String, String> dynamicVariablesFromOperand = getDynamicVariablesFromOperand(operand);
                if (dynamicVariablesFromOperand != null) {
                    dynamicVariables.putAll(getDynamicVariablesFromOperand(operand));
                }
                else{
                    logger.debug("The base metric {} does not exist in the base metrics", operand);
                    return null;
                }
            }
        }
        return dynamicVariables;
    }

    private SetMultimap<String, String> getDynamicVariablesFromOperand(String baseMetricExpression){
        MetricNameFetcher metricNameFetcher = new MetricNameFetcher();
        String baseMetricname = metricNameFetcher.getMetricName(baseMetricExpression);
        Map<String, BigDecimal> matchingBaseMetricMap = organisedBaseMetricsMap.get(baseMetricname);
        if(matchingBaseMetricMap != null) {
            SetMultimap<String, String> globalMultiMap = HashMultimap.create();
            for (Map.Entry<String, BigDecimal> baseMetric : matchingBaseMetricMap.entrySet()) {
                String baseMetricPath = baseMetric.getKey();
                if (!Strings.isNullOrEmpty(baseMetricPath) && !Strings.isNullOrEmpty(baseMetricExpression)) {
                    List<String> baseMetricExpressionList = splitters.getPipeSplitter().splitToList(baseMetricExpression);
                    List<String> baseMetricPathList = splitters.getPipeSplitter().splitToList(baseMetricPath);
                    if (baseMetricExpressionList.size() == baseMetricPathList.size()) {
                        SetMultimap<String, String> localMultiMap = splitAndPopulateVariablesMap(baseMetricExpressionList, baseMetricPathList);
                        if (localMultiMap != null) {
                            globalMultiMap.putAll(localMultiMap);
                        }
                    }
                }
            }
            return globalMultiMap;
        }
        else{
            return null;
        }
    }

    private SetMultimap<String, String> splitAndPopulateVariablesMap(List<String> baseMetricExpressionList, List<String> baseMetricPathList){
        SetMultimap<String, String> localMultiMap = HashMultimap.create();
        Iterator expressionIterator = baseMetricExpressionList.iterator();
        Iterator pathIterator = baseMetricPathList.iterator();
        while(expressionIterator.hasNext() && pathIterator.hasNext()){
            String expressionValue = expressionIterator.next().toString();
            String nameValue = pathIterator.next().toString();
            if(expressionValue.startsWith("{") && expressionValue.endsWith("}")){
                localMultiMap.put(expressionValue, nameValue);
            }
            else if(!expressionValue.equals(nameValue)){
                return null;
            }
        }
        return localMultiMap;
    }
}
