package com.appdynamics.extensions.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/10/17.
 * This class takes the list of derived metrics(with metric properties) from the "derived" section
 * in config.yml and also baseMetricsMap(with metricNames and metricValues in BigDecimal).
 * The calculateDerivedMetrics() method will calculate the derived metrics values and
 * return a map (with derived metricNames and their metricvalues in BigDecimal).
 */
public class DerivedMetricsCalculator {
    public Map<String, BigDecimal> baseMetricsMap = Maps.newConcurrentMap();
    private List<Map<String, ?>> derivedMetricsList;
    private String metricPrefix;

    public DerivedMetricsCalculator(List<Map<String, ?>> derivedMetricsList, String metricPrefix){
        this.derivedMetricsList = derivedMetricsList;
        this.metricPrefix = metricPrefix;
    }

    public void addToBaseMetricsMap(String metricPath, String metricValue){
        baseMetricsMap.put(metricPath, new BigDecimal(metricValue));
    }

    public Map<String, MetricProperties> calculateAndReturnDerivedMetrics(){
        Map<String, MetricProperties> derivedMetricsMap = Maps.newHashMap();
        for(Map<String, ?> derivedMetric : derivedMetricsList){
            String metricName = derivedMetric.entrySet()
                    .iterator()
                    .next()
                    .getKey();
            Map<String, ?> derivedMetricProperties = (Map<String, ?>)derivedMetric.entrySet().iterator().next().getValue();
            String metricPath = derivedMetricProperties.get("metricPath").toString();
            String formula = derivedMetricProperties.get("formula").toString();
            IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(baseMetricsMap, formula, metricPrefix);
            SetMultimap<String, String> dynamicPathsMap = individualDerivedMetricProcessor.processDerivedMetric();


        }
        return derivedMetricsMap;
    }


}

