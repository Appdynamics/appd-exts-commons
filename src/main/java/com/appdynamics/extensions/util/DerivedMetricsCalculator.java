package com.appdynamics.extensions.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by venkata.konala on 8/10/17.
 * This class takes the list of derived metrics(with metric properties) from the "derived" section
 * in config.yml and also baseMetricsMap(with metricNames and metricValues in BigDecimal).
 * The calculateDerivedMetrics() method will calculate the derived metrics values and
 * return a map (with derived metricNames and their metricvalues in BigDecimal).
 */
public class DerivedMetricsCalculator {
    List<Map<String, ?>> derivedMetricsList;
    Map<String, BigDecimal> baseMetricsMap = Maps.newConcurrentMap();
    String metricPrefix;

    public DerivedMetricsCalculator(List<Map<String, ?>> derivedMetricsList, String metricPrefix){
        this.derivedMetricsList = derivedMetricsList;
        this.metricPrefix = metricPrefix;
    }

    public void addToBaseMetricsMap(String metricPath, String metricValue){
        baseMetricsMap.put(metricPath, new BigDecimal(metricValue));
    }

    public Map<String, MetricProperties> calculateAndReturnDerivedMetrics(){
        Map<String, Map<String, BigDecimal>> organisedMetricsMap = buildOrganisedBaseMetricsMap();
        Map<String, MetricProperties> derivedMetricsMap = Maps.newHashMap();
        for(Map<String, ?> derivedMetricMapFromConfig: derivedMetricsList){
            if(derivedMetricMapFromConfig != null) {
                String derivedMetricName = derivedMetricMapFromConfig.entrySet().iterator().next().getKey();
                Map<String, ?> derivedMetricPropertyMap = (Map<String, ?>)derivedMetricMapFromConfig.entrySet().iterator().next().getValue();
                String formula = derivedMetricPropertyMap.get("formula").toString();
                for(Map.Entry<String, Map<String, BigDecimal>> serverMetrics : organisedMetricsMap.entrySet()){
                    String serverName = serverMetrics.getKey();
                    Map<String, BigDecimal> serverMetricsMap = serverMetrics.getValue();
                    BigDecimal derivedMetricValue = null;
                    if(formula != null){
                        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(serverMetricsMap, formula);
                        derivedMetricValue = expressionEvaluator.expressionEval();
                    }
                    if(derivedMetricValue != null){
                        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(derivedMetricPropertyMap,derivedMetricName, derivedMetricValue.toString());
                        MetricProperties derivedMetricProperties = metricPropertiesBuilder.buildMetricProperties();
                        String derivedMetricPath = metricPrefix + "|" + "derived" + "|" + serverName + "|" + derivedMetricName;
                        derivedMetricsMap.put(derivedMetricPath, derivedMetricProperties);
                    }
                }
            }
        }
        return derivedMetricsMap;
    }

    private Map<String, Map<String, BigDecimal>> buildOrganisedBaseMetricsMap(){
        Map<String, Map<String, BigDecimal>> organisedMetricsMap = Maps.newHashMap();
        for(Map.Entry<String, BigDecimal> baseMetric : baseMetricsMap.entrySet()){
            String metricPath = baseMetric.getKey();
            BigDecimal metricValue = baseMetric.getValue();
            String serverName = retrieveServerName(metricPath);
            String metricName = retrieveMetricName(metricPath);
            if(organisedMetricsMap.containsKey(serverName)){
                Map<String, BigDecimal> individualServerMetricMap = organisedMetricsMap.get(serverName);
                individualServerMetricMap.put(metricName, metricValue);
            }
            else{
                Map<String, BigDecimal> individualServerMetricMap = Maps.newHashMap();
                individualServerMetricMap.put(metricName, metricValue);
                organisedMetricsMap.put(serverName, individualServerMetricMap);
            }
        }
        return organisedMetricsMap;
    }

    private String retrieveServerName(String metricPath){
        return "";
    }

    //#TODO get the metric name from metric path.
    private String retrieveMetricName(String metricPath){
        Splitter pipeSplitter = Splitter.on('|')
                .omitEmptyStrings()
                .trimResults();
        List<String> metric = pipeSplitter.splitToList(metricPath);
        if(!metric.isEmpty()){
            return metric.get(metric.size() - 1);
        }
        return "";
    }
}

