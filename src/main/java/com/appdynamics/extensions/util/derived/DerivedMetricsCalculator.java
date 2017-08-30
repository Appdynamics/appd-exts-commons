package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.MetricProperties;
import com.appdynamics.extensions.util.MetricPropertiesBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DerivedMetricsCalculator.class);
    private Map<String, BigDecimal> baseMetricsMap = Maps.newConcurrentMap();
    private List<Map<String, ?>> derivedMetricsList;
    private String metricPrefix;
    private MetricNameFetcher metricNameFetcher = new MetricNameFetcher();

    public DerivedMetricsCalculator(List<Map<String, ?>> derivedMetricsList, String metricPrefix){
        this.derivedMetricsList = derivedMetricsList;
        this.metricPrefix = getMetricPrefix(metricPrefix);
    }

    private String getMetricPrefix(String metricPrefix){
        if(!metricPrefix.endsWith("|")){
            metricPrefix = metricPrefix + "|";
        }
        return metricPrefix;
    }

    public void addToBaseMetricsMap(String metricPath, String metricValue){
        baseMetricsMap.put(metricPath, new BigDecimal(metricValue));
    }

    public Multimap<String, MetricProperties> calculateAndReturnDerivedMetrics(){
        long startTime = System.currentTimeMillis();
        Multimap<String, MetricProperties> derivedMetricsMap = ArrayListMultimap.create();
        if(baseMetricsMap != null) {
            Map<String, Map<String, BigDecimal>> organisedMap = buildOrganisedMap(baseMetricsMap);
            for (Map<String, ?> derivedMetric : derivedMetricsList) {
                String derivedMetricPathFromConfig =  derivedMetric.get("derivedMetricPath") == null ? null : derivedMetric.get("derivedMetricPath").toString();
                String formula = derivedMetric.get("formula") == null ? null : derivedMetric.get("formula").toString();
                if (!Strings.isNullOrEmpty(derivedMetricPathFromConfig) && !Strings.isNullOrEmpty(formula)) {
                    IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedMap, derivedMetricPathFromConfig, formula);
                    Multimap<String, BigDecimal> individualDerivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
                    for (Map.Entry<String, BigDecimal> entry : individualDerivedMetricMap.entries()) {
                        StringBuilder derivedMetricPath = getMetricPathWithMetricPrefix(entry.getKey());
                        String derivedMetricName = metricNameFetcher.getMetricName(derivedMetricPath.toString());
                        String derivedMetricValue = entry.getValue().toString();
                        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(derivedMetric, derivedMetricName, derivedMetricValue);
                        MetricProperties metricProperties = metricPropertiesBuilder.buildMetricProperties();
                        derivedMetricPath = applyAlias(derivedMetricPath.toString(), derivedMetricName, metricProperties.getAlias());
                        derivedMetricsMap.put(derivedMetricPath.toString(), metricProperties);
                        logger.debug("Adding {} with value {} to the multimap", derivedMetricPath, metricProperties.getMetricValue());
                    }
                    }
                else {
                    logger.debug("The derived metric {} does not have derivedMetricPath and formula fields specified in the config.yml", derivedMetric);
                }
            }
            long endTime = System.currentTimeMillis();
            logger.debug("Total time taken to calculate and return derived metrics is {} ms",endTime - startTime);
            return derivedMetricsMap;
        }
        else{
            logger.debug("There are no base metrics to calculate derived metrics");
            return derivedMetricsMap;
        }
    }

    private Map<String, Map<String, BigDecimal>> buildOrganisedMap(Map<String, BigDecimal> baseMetricsMap){
        Map<String, Map<String, BigDecimal>> organisedMap = Maps.newHashMap();
        for(Map.Entry<String, BigDecimal> baseMetric : baseMetricsMap.entrySet()){
            String key = baseMetric.getKey();
            String pathWithoutPrefix = key.replace(metricPrefix, "");
            BigDecimal value = baseMetric.getValue();
            if(key != null && value != null){
                String metricName = metricNameFetcher.getMetricName(key);
                if(organisedMap.containsKey(metricName)){
                    Map<String, BigDecimal> organisedMapValue = organisedMap.get(metricName);
                    organisedMapValue.put(pathWithoutPrefix, value);
                }
                else{
                    Map<String, BigDecimal> organisedMapValue = Maps.newHashMap();
                    organisedMapValue.put(pathWithoutPrefix, value);
                    organisedMap.put(metricName, organisedMapValue);
                }
            }
        }
        return organisedMap;
    }

    private StringBuilder getMetricPathWithMetricPrefix(String metricPath){
        StringBuilder derivedMetricPath = new StringBuilder(metricPrefix);
        derivedMetricPath.append(metricPath);
        return derivedMetricPath;
    }

    private StringBuilder applyAlias(String derivedMetricPath, String derivedMetricName, String alias){
        return new StringBuilder(derivedMetricPath
                .replace(derivedMetricName, alias));
    }
}

