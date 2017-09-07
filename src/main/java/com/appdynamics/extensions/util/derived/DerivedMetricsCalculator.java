package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.util.Metric;
import com.appdynamics.extensions.util.MetricProperties;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private DerivedMetricsPathHandler pathHandler = new DerivedMetricsPathHandler();
    private String metricPrefix;

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

    public Multimap<String, Metric> calculateAndReturnDerivedMetrics(){
        long startTime = System.currentTimeMillis();
        Multimap<String, Metric> derivedMetricsMap = ArrayListMultimap.create();
        if(baseMetricsMap.size() != 0) {
            Map<String, Map<String, BigDecimal>> organisedMap = buildDataStructure(baseMetricsMap);
            clearBaseMetricsMap();
            for (Map<String, ?> derivedMetric : derivedMetricsList) {
                String derivedMetricPathFromConfig =  derivedMetric.get("derivedMetricPath") == null ? null : derivedMetric.get("derivedMetricPath").toString();
                String formula = derivedMetric.get("formula") == null ? null : derivedMetric.get("formula").toString();
                if (!Strings.isNullOrEmpty(derivedMetricPathFromConfig) && !Strings.isNullOrEmpty(formula)) {
                    IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedMap, derivedMetricPathFromConfig, formula, pathHandler);
                    try {
                        Multimap<String, BigDecimal> individualDerivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
                        for (Map.Entry<String, BigDecimal> entry : individualDerivedMetricMap.entries()) {
                            StringBuilder derivedMetricPath = pathHandler.getMetricPathWithMetricPrefix(entry.getKey(),metricPrefix);
                            String derivedMetricName = pathHandler.getMetricName(derivedMetricPath.toString());
                            BigDecimal derivedMetricValue = entry.getValue();
                            Metric metric = new Metric(derivedMetricName, derivedMetricValue.toString(), derivedMetricPath.toString(), derivedMetric);
                            MetricProperties metricProperties = metric.getMetricProperties();
                            derivedMetricPath = pathHandler.applyAlias(derivedMetricPath.toString(), derivedMetricName, metricProperties.getAlias());
                            derivedMetricsMap.put(derivedMetricPath.toString(), metric);
                            logger.debug("Adding {} with value {} to the multimap", derivedMetricPath, metric.getMetricValue());
                        }
                    }
                    catch(MetricNotFoundException e){
                        logger.debug(e.toString());
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

    private Map<String, Map<String, BigDecimal>> buildDataStructure(Map<String, BigDecimal> baseMetricsMap){
        Map<String, Map<String, BigDecimal>> organisedMap = Maps.newHashMap();
        for(Map.Entry<String, BigDecimal> baseMetric : baseMetricsMap.entrySet()){
            String key = baseMetric.getKey();
            String pathWithoutPrefix = key.replace(metricPrefix, "");
            BigDecimal value = baseMetric.getValue();
            if(key != null && value != null){
                String metricName = pathHandler.getMetricName(key);
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

    private void clearBaseMetricsMap(){
        baseMetricsMap.clear();
    }

}

