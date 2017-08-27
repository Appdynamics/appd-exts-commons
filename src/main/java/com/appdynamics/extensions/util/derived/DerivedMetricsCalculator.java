package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.MetricProperties;
import com.appdynamics.extensions.util.MetricPropertiesBuilder;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/10/17.
 * This class takes the list of derived metrics(with metric properties) from the "derived" section
 * in config.yml. It gets the baseMetricsMap(with metricNames and metricValues in BigDecimal) from
 * printMetric() methods in MetricWriteHelper.java
 * The calculateAndReturnDerivedMetrics() method will calculate the derived metrics values and
 * return a Multimap (with derived metricPaths and their MetricProperties).
 */
public class DerivedMetricsCalculator {
    public Map<String, BigDecimal> baseMetricsMap = Maps.newConcurrentMap();
    private List<Map<String, ?>> derivedMetricsList;
    private String metricPrefix;
    private Splitter pipeSplitter = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public DerivedMetricsCalculator(List<Map<String, ?>> derivedMetricsList, String metricPrefix){
        this.derivedMetricsList = derivedMetricsList;
        this.metricPrefix = metricPrefix;
    }

    public void addToBaseMetricsMap(String metricPath, String metricValue){
        baseMetricsMap.put(metricPath, new BigDecimal(metricValue));
    }

    public Multimap<String, MetricProperties> calculateAndReturnDerivedMetrics(){
        if(!metricPrefix.endsWith("|")){
            metricPrefix = metricPrefix + "|";
        }
        //This map has to be ArrayList Multimap so that all values of a particular key are retained. If HashSet Multimap is used, duplicate values are removed.
        Multimap<String, MetricProperties> derivedMetricsMap = ArrayListMultimap.create();
        for(Map<String, ?> derivedMetric : derivedMetricsList){
            //venkata.konala what is this rawDerivedMetricName? Why is there no sample config.yaml against which you have performed your test?
            String rawDerivedMetricName = derivedMetric.entrySet().iterator().next().getKey();
            Map<String, ?> derivedMetricPropertiesFromConfig = (Map<String, ?>)derivedMetric.entrySet().iterator().next().getValue();
            if(derivedMetricPropertiesFromConfig != null) {
                String rawDerivedMetricPath = derivedMetricPropertiesFromConfig.get("derivedMetricPath") == null ? null : derivedMetricPropertiesFromConfig.get("derivedMetricPath").toString();
                String formula = derivedMetricPropertiesFromConfig.get("formula") == null ? null : derivedMetricPropertiesFromConfig.get("formula").toString();
                if (rawDerivedMetricPath != null && formula != null) {
                    IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(baseMetricsMap, metricPrefix, rawDerivedMetricName, rawDerivedMetricPath, formula);
                    Multimap<String, BigDecimal> individualDerivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
                    for (Map.Entry<String, BigDecimal> entry : individualDerivedMetricMap.entries()) {
                        String derivedMetricPath = entry.getKey();
                        BigDecimal derivedMetricValueBigD = entry.getValue();
                        if (derivedMetricPath != null && derivedMetricValueBigD != null) {
                            String derivedMetricName = getMetricName(derivedMetricPath);
                            String derivedMetricValue = derivedMetricValueBigD.toString();
                            MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(derivedMetricPropertiesFromConfig, derivedMetricName, derivedMetricValue);
                            MetricProperties metricProperties = metricPropertiesBuilder.buildMetricProperties();
                            derivedMetricsMap.put(derivedMetricPath, metricProperties);
                        }
                    }
                }
                //@venkata.konala else log a warn
            }
        }
        return derivedMetricsMap;
    }

    public String getMetricName(String derivedMetricPath){
        List<String> splitList = pipeSplitter.splitToList(derivedMetricPath);
        if(splitList.size() > 0){
            return splitList.get(splitList.size() - 1);
        }
        else{
            return null;
        }
    }
}

