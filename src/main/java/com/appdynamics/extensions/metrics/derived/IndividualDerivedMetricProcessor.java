package com.appdynamics.extensions.metrics.derived;

import com.appdynamics.extensions.util.MetricPathUtils;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;


/**
 * Created by venkata.konala on 8/23/17.
 */
class IndividualDerivedMetricProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IndividualDerivedMetricProcessor.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private String metricPath;
    private OperandsHandler operandsHandler;
    private DynamicVariablesProcessor dynamicVariablesProcessor;
    private SetMultimap<String, String> dynamicvariables;
    private DerivedMetricsPathHandler pathHandler;

    IndividualDerivedMetricProcessor(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, String metricPath, String formula,DerivedMetricsPathHandler pathHandler){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.metricPath = metricPath;
        this.pathHandler = pathHandler;
        this.operandsHandler = new OperandsHandler(formula,pathHandler);
    }

    Multimap<String, BigDecimal> processDerivedMetric() throws MetricNotFoundException{
        long startTime = System.currentTimeMillis();
        dynamicVariablesProcessor = new DynamicVariablesProcessor(organisedBaseMetricsMap, operandsHandler.getBaseOperands(),pathHandler);
        dynamicvariables = dynamicVariablesProcessor.getDynamicVariables();
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(organisedBaseMetricsMap, dynamicvariables, metricPath, operandsHandler,pathHandler);
        Multimap<String, BigDecimal> derivedMetricMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        long endTime = System.currentTimeMillis();
        logger.debug("Time taken to calculate {} metric is {} ms", MetricPathUtils.getMetricName(metricPath), endTime - startTime);
        return derivedMetricMap;
    }
}
