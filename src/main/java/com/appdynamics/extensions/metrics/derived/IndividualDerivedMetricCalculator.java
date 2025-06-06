/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.util.NumberUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Created by venkata.konala on 8/23/17.
 */
class IndividualDerivedMetricCalculator {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(IndividualDerivedMetricCalculator.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private OperandsHandler operandsHandler;
    private String metricPath;
    private SetMultimap<String, String> dynamicVariables;
    private DerivedMetricsPathHandler pathHandler;
    private Multimap<String, BigDecimal> derivedMetricMap = ArrayListMultimap.create();

    IndividualDerivedMetricCalculator(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, SetMultimap<String, String> dynamicVariables, String metricPath, OperandsHandler operandsHandler, DerivedMetricsPathHandler pathHandler) {
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.dynamicVariables = dynamicVariables;
        this.metricPath = metricPath;
        this.operandsHandler = operandsHandler;
        this.pathHandler = pathHandler;
    }

    Multimap<String, BigDecimal> calculateDerivedMetric() {
        substitute(metricPath, operandsHandler.getBaseOperands(), dynamicVariables);
        return derivedMetricMap;
    }

    void substitute(String path, Set<String> localOperands, SetMultimap<String, String> dynamicvariables) {
        String variable = operandsHandler.checkForFirstVariable(localOperands);
        if (variable == null) {
            String substitutedFormula = getValueSubstitutedFormula(localOperands);
            if (substitutedFormula != null) {
                try {
                    long startTime = System.currentTimeMillis();
                    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(substitutedFormula);
                    BigDecimal value = expressionEvaluator.eval();
                    long endTime = System.currentTimeMillis();
                    logger.debug("The time taken to calculate the formula {} : {} ms", substitutedFormula, endTime - startTime);
                    if (value != null) {
                        derivedMetricMap.put(path, value);
                    }
                } catch (IllegalExpressionException e) {
                    logger.debug(e.toString());
                }
            }
            return;
        }
        Set<String> variableValues = dynamicvariables.get(variable);
        for (String variableValue : variableValues) {
            Set<String> modifiedOperands = operandsHandler.getSubstitutedOperands(localOperands, variable, variableValue);
            String modifiedPath = pathHandler.getSubstitutedPath(path, variable, variableValue);
            substitute(modifiedPath, modifiedOperands, dynamicvariables);
        }

    }

    private String getValueSubstitutedFormula(Set<String> modifiedOperands) {
        String modifiedExpressionWithoutValues = operandsHandler.getSubstitutedExpression(modifiedOperands);
        String modifiedExpressionWithValues = modifiedExpressionWithoutValues;
        Iterator<String> modifiedOperandsIterator = modifiedOperands.iterator();
        while (modifiedOperandsIterator.hasNext()) {
            String baseMetric = modifiedOperandsIterator.next();
            if (NumberUtils.isNumber(baseMetric)) {
                continue;
            }
            String baseMetricName = MetricPathUtils.getMetricName(baseMetric);
            Map<String, BigDecimal> baseMetricMap = organisedBaseMetricsMap.get(baseMetricName);
            BigDecimal baseMetricValue = baseMetricMap.get(baseMetric);
            if (baseMetricValue != null) {
                modifiedExpressionWithValues = modifiedExpressionWithValues.replace(baseMetric, String.valueOf(baseMetricValue.doubleValue()));
            } else {//The baseMetric is either not present in the the metricMap or its value is null
                logger.debug("The baseMetric {} in the expression {} does not exist in the baseMetricsMap", baseMetric, modifiedExpressionWithoutValues);
                return null;
            }
        }
        logger.debug("The expression {} has been evaluated as {}", modifiedExpressionWithoutValues, modifiedExpressionWithValues);
        return modifiedExpressionWithValues;
    }
}
