package com.appdynamics.extensions.util;

import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 8/10/17.
 * This class takes the list of derived metrics(with metric properties) from the "derived" section
 * in config.yml and also baseMetricsMap(with metricNames and metricValues in BigDecimal).
 * The calculateDerivedMetrics() method will calculate the derived metrics values and
 * return a map (with derived metricNames and their metricvalues in BigDecimal).
 */
public class DerivedMetricsCalculator {
    List<Map<String, ?>> derivedMetricsList;
    Map<String, BigDecimal> baseMetricsMap = Maps.newHashMap();

    public DerivedMetricsCalculator(List<Map<String, ?>> derivedMetricsList){
        this.derivedMetricsList = derivedMetricsList;
    }

    public void addToBaseMetricsMap(String metricPath, String metricValue){
        baseMetricsMap.put(metricPath, new BigDecimal(metricValue));
    }
    //#TODO get the metric name from metric path.
    private String getMetricName(String metricPath){
        return "";
    }

    public Map<String, MetricProperties> calculateAndReturnDerivedMetrics(){
        Map<String, MetricProperties> derivedMetricsMap = Maps.newHashMap();
        for(Map<String, ?> derivedMetricMapFromConfig: derivedMetricsList){
            if(derivedMetricMapFromConfig != null) {
                String derivedMetricPath = derivedMetricMapFromConfig.entrySet().iterator().next().getKey();
                String derivedMetricName = getMetricName(derivedMetricPath);
                Map<String, ?> derivedMetricPropertyMap = (Map<String, ?>)derivedMetricMapFromConfig.entrySet().iterator().next().getValue();
                String formula = derivedMetricPropertyMap.get("formula").toString();
                BigDecimal derivedMetricValue = null;
                if(formula != null){
                    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(baseMetricsMap, formula);
                    derivedMetricValue = expressionEvaluator.expressionEval();
                }
                if(derivedMetricValue != null){
                    MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(derivedMetricPropertyMap,derivedMetricName, derivedMetricPath, derivedMetricValue.toString());
                    MetricProperties derivedMetricProperties = metricPropertiesBuilder.buildMetricProperties();

                    derivedMetricsMap.put(derivedMetricName, derivedMetricProperties);
                }
            }
        }
        return derivedMetricsMap;
    }
}

