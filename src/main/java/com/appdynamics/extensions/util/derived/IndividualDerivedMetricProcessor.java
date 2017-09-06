package com.appdynamics.extensions.util.derived;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.*;
import static com.appdynamics.extensions.util.derived.Constants.metricNameFetcher;

/**
 * Created by venkata.konala on 8/23/17.
 */
class IndividualDerivedMetricProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IndividualDerivedMetricProcessor.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private String metricPath;
    private Operand operand;
    private DynamicVariablesProcessor dynamicVariablesProcessor;
    private SetMultimap<String, String> dynamicvariables;

    IndividualDerivedMetricProcessor(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, String metricPath, String formula){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.metricPath = metricPath;
        this.operand = new Operand(formula);
    }

     Multimap<String, BigDecimal> processDerivedMetric() throws MetricNotFoundException{
        long startTime = System.currentTimeMillis();
        dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operand.getBaseOperands());
        dynamicvariables = dynamicVariablesProcessor.getDynamicVariables();
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(organisedBaseMetricsMap, dynamicvariables, metricPath, operand);
        Multimap<String, BigDecimal> derivedMetricMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        long endTime = System.currentTimeMillis();
        logger.debug("Time taken to calculate {} metric is {} ms", metricNameFetcher.getMetricName(metricPath), endTime - startTime);
        return derivedMetricMap;
    }
}
