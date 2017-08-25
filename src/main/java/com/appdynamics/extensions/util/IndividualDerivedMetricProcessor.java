package com.appdynamics.extensions.util;

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
    private SetMultimap<String, String> globalMultiMap = HashMultimap.create();
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
        Set<String> baseMetrics = getBaseMetricsFromFormula(formula);
        for(String baseMetric : baseMetrics){
            populateGlobalMultiMap(baseMetric);
        }
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(baseMetricsMap, metricPrefix, metricName, metricPath, formula, baseMetrics, globalMultiMap);
        return individualDerivedMetricCalculator.calculateDerivedMetric();
    }

    /* The getBaseMetricsfromFormula(String formula) method takes an expression and
     * returns the set of operands. The operators allowed are +,-,*,/,%,^.
     * To apply precedence, "()" can be used. Spaces can be used in the expression
     * to separate operators and operands. Please note that the operands do not
     * have to be only baseMetrics, they can be numbers also.
     */
    public Set<String> getBaseMetricsFromFormula(String formula){
        Set<String> baseMetricsSet = new HashSet<String>();
        Splitter splitter = Splitter.on(CharMatcher.anyOf("(+-*/%^) "))
                .trimResults()
                .omitEmptyStrings();
        List<String> baseMetricsList = splitter.splitToList(formula);
        for(String baseMetric: baseMetricsList){
            baseMetricsSet.add(baseMetric);
        }
        return baseMetricsSet;
    }

    public void populateGlobalMultiMap(String baseMetricExpression){
        for(Map.Entry<String, BigDecimal> baseMetric: baseMetricsMap.entrySet()){
            String baseMetricPath = baseMetric.getKey();
            baseMetricPath = baseMetricPath.replace(metricPrefix,"");
            if(!Strings.isNullOrEmpty(baseMetricPath) && !Strings.isNullOrEmpty(baseMetricExpression)) {
                List<String> baseMetricExpressionList = pipeSplitter.splitToList(baseMetricExpression);
                List<String> baseMetricPathList = pipeSplitter.splitToList(baseMetricPath);
                if (baseMetricExpressionList.size() == baseMetricPathList.size()) {
                    SetMultimap<String, String> localMultiMap = splitAndPopulateLocalMap(baseMetricExpressionList, baseMetricPathList);
                    if (localMultiMap != null) {
                        globalMultiMap.putAll(localMultiMap);
                    }
                }
            }
        }
    }

    public SetMultimap<String, String> splitAndPopulateLocalMap(List<String> baseMetricExpressionList, List<String> baseMetricPathList){
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

    public SetMultimap<String, String> getGlobalMultiMap(){
        return globalMultiMap;
    }
}
