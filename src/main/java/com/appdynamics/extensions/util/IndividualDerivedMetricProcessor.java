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
    private String formula;
    private String metricPrefix;
    private SetMultimap<String, String> globalMultiMap = HashMultimap.create();
    private Splitter pipeSplitter = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public IndividualDerivedMetricProcessor(Map<String, BigDecimal> baseMetricsMap, String formula, String metricPrefix){
        this.baseMetricsMap = baseMetricsMap;
        this.formula = formula;
        this.metricPrefix = metricPrefix;
    }

    public Multimap<String, MetricProperties> processDerivedMetric(){
        Set<String> baseMetrics = getBaseMetricsFromFormula(formula);
        for(String baseMetric : baseMetrics){
            populateGlobalMultiMap(baseMetric);
        }
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator();
        return individualDerivedMetricCalculator.calculateDerivedMetric();
    }

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
            String baseMetricName = baseMetric.getKey();
            baseMetricName = baseMetricName.replace(metricPrefix,"");
            if(!Strings.isNullOrEmpty(baseMetricName) && !Strings.isNullOrEmpty(baseMetricExpression)) {
                List<String> baseMetricExpressionList = pipeSplitter.splitToList(baseMetricExpression);
                List<String> baseMetricNameList = pipeSplitter.splitToList(baseMetricName);
                if (baseMetricExpressionList.size() == baseMetricNameList.size()) {
                    Multimap<String, String> localMap = splitAndPopulateLocalMap(baseMetricExpressionList, baseMetricNameList);
                    if (localMap != null) {
                        globalMultiMap.putAll(localMap);
                    }
                }
            }
        }
    }

    public SetMultimap<String, String> splitAndPopulateLocalMap(List<String> baseMetricExpressionList, List<String> baseMetricNameList){
        SetMultimap<String, String> localMultiMap = HashMultimap.create();
        Iterator expressionIterator = baseMetricExpressionList.iterator();
        Iterator nameIterator = baseMetricNameList.iterator();
        while(expressionIterator.hasNext() && nameIterator.hasNext()){
            String expressionValue = expressionIterator.next().toString();
            String nameValue = nameIterator.next().toString();
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
