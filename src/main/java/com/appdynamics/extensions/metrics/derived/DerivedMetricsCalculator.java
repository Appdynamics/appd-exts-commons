/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.metrics.derived;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 8/10/17.
 * This class takes the list of derived metrics(with metric properties) from the "derivedMetrics" section
 * in config.yml. It gets the baseMetricsMap(with metricPath and metricValues) from
 * printMetric() methods in MetricWriteHelper.java
 * The calculateAndReturnDerivedMetrics() method will calculate the derived metrics values and
 * return a List<Metric>.
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

    public void clearBaseMetricsMap(){
        baseMetricsMap.clear();
    }

    public List<Metric> calculateAndReturnDerivedMetrics(){
        long startTime = System.currentTimeMillis();
        List<Metric> metricList = Lists.newArrayList();
        if(baseMetricsMap.size() != 0) {
            Map<String, Map<String, BigDecimal>> organisedMap = buildDataStructure(baseMetricsMap);
            for (Map<String, ?> derivedMetric : this.derivedMetricsList) {
                String derivedMetricPathFromConfig =  derivedMetric.get("derivedMetricPath") == null ? null : derivedMetric.get("derivedMetricPath").toString();
                String formula = derivedMetric.get("formula") == null ? null : derivedMetric.get("formula").toString();
                if (!Strings.isNullOrEmpty(derivedMetricPathFromConfig) && !Strings.isNullOrEmpty(formula)) {
                    IndividualDerivedMetricProcessor individualDerivedMetricProcessor = new IndividualDerivedMetricProcessor(organisedMap, derivedMetricPathFromConfig, formula, pathHandler);
                    try {
                        Multimap<String, BigDecimal> individualDerivedMetricMap = individualDerivedMetricProcessor.processDerivedMetric();
                        for (Map.Entry<String, BigDecimal> entry : individualDerivedMetricMap.entries()) {
                            StringBuilder derivedMetricPath = pathHandler.getMetricPathWithMetricPrefix(entry.getKey(),metricPrefix);
                            String derivedMetricName = MetricPathUtils.getMetricName(derivedMetricPath.toString());
                            BigDecimal derivedMetricValue = entry.getValue();
                            Metric metric = new Metric(derivedMetricName, derivedMetricValue.toString(), derivedMetricPath.toString(), derivedMetric);
                            metricList.add(metric);
                            logger.debug("Adding {} with value {} to the list of Metric", derivedMetricPath, metric.getMetricValue());
                        }
                    }
                    catch(MetricNotFoundException e){
                        logger.debug(e.toString());
                    }
                }
                else {
                    logger.debug("The derived metric {} does not have some or all of these fields {derivedMetricPath, formula} specified in the config.yml", derivedMetric);
                }
            }
            long endTime = System.currentTimeMillis();
            logger.debug("Total time taken to calculate and return derived metrics is {} ms",endTime - startTime);
            return metricList;
        }
        else{
            logger.debug("There are no base metrics to calculate derived metrics");
            return metricList;
        }
    }

    private Map<String, Map<String, BigDecimal>> buildDataStructure(Map<String, BigDecimal> baseMetricsMap){
        Map<String, Map<String, BigDecimal>> organisedMap = Maps.newHashMap();
        for(Map.Entry<String, BigDecimal> baseMetric : baseMetricsMap.entrySet()){
            String key = baseMetric.getKey();
            String pathWithoutPrefix = key.replace(metricPrefix, "");
            BigDecimal value = baseMetric.getValue();
            if(key != null && value != null){
                String metricName = MetricPathUtils.getMetricName(key);
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
}

