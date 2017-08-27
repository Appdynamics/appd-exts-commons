package com.appdynamics.extensions.util.derived;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/23/17.
 */
public class IndividualDerivedMetricProcessor {
    private Map<String, BigDecimal> baseMetricsMap;
    private String metricPrefix;
    String metricName;
    String metricPath;
    private String formula;
    private SetMultimap<String, String> variablesMultiMap = HashMultimap.create();
    private Splitter pipeSplitter = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public IndividualDerivedMetricProcessor(Map<String, BigDecimal> baseMetricsMap, String metricPrefix, String metricName, String metricPath, String formula){
        this.baseMetricsMap = baseMetricsMap;
        this.metricPrefix = metricPrefix;
        this.metricName = metricName;
        this.metricPath = metricPath;
        this.formula = formula;
    }

    public Multimap<String, BigDecimal> processDerivedMetric(){
        Set<String> baseMetrics = getOperandsFromFormula(formula);
        //venkata.konala ..Can you optimize this? Imagine you have 10k base metrics being reported and 2 operands from derived metrics formulae.
        //You will be unnecessarily looping through the 10k metrics twice. You already know that no.of base metrics >> no. of operands.
        //Can you interchange the for loops? Will this not optimize the time taken to execute?
        for(String baseMetric : baseMetrics){
            populateVariablesMultiMap(baseMetric);
        }
        //venkata.konala This is so ugly
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(baseMetricsMap, metricPrefix, metricName, metricPath, formula, baseMetrics, variablesMultiMap);
        return individualDerivedMetricCalculator.calculateDerivedMetric();
    }

    /* The getBaseMetricsfromFormula(String formula) method takes an expression and
     * returns the set of operands. The operators allowed are +,-,*,/,%,^.
     * To apply precedence, "()" can be used. Spaces can be used in the expression
     * to separate operators and operands. Please note that the operands do not
     * have to be only baseMetrics, they can be numbers also.
     */
    public Set<String> getOperandsFromFormula(String formula){
        Set<String> operands = new HashSet<String>();
        Splitter splitter = Splitter.on(CharMatcher.anyOf("(+-*/%^) "))
                .trimResults()
                .omitEmptyStrings();
        List<String> baseMetricsList = splitter.splitToList(formula);
        for(String baseMetric: baseMetricsList){
            operands.add(baseMetric);
        }
        return operands;
    }

    public void populateVariablesMultiMap(String baseMetricExpression){
        for(Map.Entry<String, BigDecimal> baseMetric: baseMetricsMap.entrySet()){
            String baseMetricPath = baseMetric.getKey();
            baseMetricPath = baseMetricPath.replace(metricPrefix,"");
            if(!Strings.isNullOrEmpty(baseMetricPath) && !Strings.isNullOrEmpty(baseMetricExpression)) {
                List<String> baseMetricExpressionList = pipeSplitter.splitToList(baseMetricExpression);
                List<String> baseMetricPathList = pipeSplitter.splitToList(baseMetricPath);
                if (baseMetricExpressionList.size() == baseMetricPathList.size()) {
                    SetMultimap<String, String> localMultiMap = splitAndPopulateVariablesMap(baseMetricExpressionList, baseMetricPathList);
                    if (localMultiMap != null) {
                        variablesMultiMap.putAll(localMultiMap);
                    }
                }
            }
        }
    }

    public SetMultimap<String, String> splitAndPopulateVariablesMap(List<String> baseMetricExpressionList, List<String> baseMetricPathList){
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

    public SetMultimap<String, String> getVariablesMultiMap(){
        return variablesMultiMap;
    }
}
