package com.appdynamics.extensions.util.derived;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by venkata.konala on 8/23/17.
 */
public class IndividualDerivedMetricProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IndividualDerivedMetricProcessor.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private String metricPath;
    private String formula;
    private MetricNameFetcher metricNameFetcher = new MetricNameFetcher();
    private OperandsFetcher operandsFetcher = new OperandsFetcher();
    private DynamicVariablesFetcher dynamicVariablesFetcher;
    private SetMultimap<String, String> dynamicvariables;
    public IndividualDerivedMetricProcessor(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, String metricPath, String formula){
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.metricPath = metricPath;
        this.formula = formula;
    }

    public Multimap<String, BigDecimal> processDerivedMetric(){
        long startTime = System.currentTimeMillis();
        Set<String> operands = operandsFetcher.getOperandsFromFormula(formula);
        dynamicVariablesFetcher = new DynamicVariablesFetcher(organisedBaseMetricsMap, operands);
        dynamicvariables = dynamicVariablesFetcher.getDynamicVariables();
        IndividualDerivedMetricCalculator individualDerivedMetricCalculator = new IndividualDerivedMetricCalculator(organisedBaseMetricsMap, dynamicvariables, metricPath, formula);
        Multimap<String, BigDecimal> derivedMetricMap = individualDerivedMetricCalculator.calculateDerivedMetric();
        long endTime = System.currentTimeMillis();
        logger.debug("Time taken to calculate {} metric is {} ms", metricNameFetcher.getMetricName(metricPath), endTime - startTime);
        return derivedMetricMap;
    }
}
