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
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.appdynamics.extensions.util.MetricPathUtils.PIPE_SPLITTER;

/**
 * Created by venkata.konala on 8/28/17.
 */
class DynamicVariablesProcessor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(DynamicVariablesProcessor.class);
    private Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap;
    private Set<String> operands;
    private DerivedMetricsPathHandler pathHandler;

    DynamicVariablesProcessor(Map<String, Map<String, BigDecimal>> organisedBaseMetricsMap, Set<String> operands, DerivedMetricsPathHandler pathHandler) {
        this.organisedBaseMetricsMap = organisedBaseMetricsMap;
        this.operands = operands;
        this.pathHandler = pathHandler;
    }

    SetMultimap<String, String> getDynamicVariables() throws MetricNotFoundException {
        SetMultimap<String, String> dynamicVariables = HashMultimap.create();
        for (String operand : operands) {
            if (!NumberUtils.isNumber(operand)) {
                SetMultimap<String, String> dynamicVariablesFromOperand = getDynamicVariablesFromOperand(operand);
                if (dynamicVariablesFromOperand.size() != 0) {
                    dynamicVariables.putAll(getDynamicVariablesFromOperand(operand));
                }
            }
        }
        return dynamicVariables;
    }

    private SetMultimap<String, String> getDynamicVariablesFromOperand(String baseMetricExpression) throws MetricNotFoundException {
        String baseMetricname = MetricPathUtils.getMetricName(baseMetricExpression);
        Map<String, BigDecimal> matchingBaseMetricMap = organisedBaseMetricsMap.get(baseMetricname);
        if (matchingBaseMetricMap != null) {
            SetMultimap<String, String> globalMultiMap = HashMultimap.create();
            for (Map.Entry<String, BigDecimal> baseMetric : matchingBaseMetricMap.entrySet()) {
                String baseMetricPath = baseMetric.getKey();
                if (!Strings.isNullOrEmpty(baseMetricPath) && !Strings.isNullOrEmpty(baseMetricExpression)) {
                    List<String> baseMetricExpressionList = PIPE_SPLITTER.splitToList(baseMetricExpression);
                    List<String> baseMetricPathList = PIPE_SPLITTER.splitToList(baseMetricPath);
                    if (baseMetricExpressionList.size() == baseMetricPathList.size()) {
                        SetMultimap<String, String> localMultiMap = splitAndPopulateVariablesMap(baseMetricExpressionList, baseMetricPathList);
                        if (localMultiMap != null) {
                            globalMultiMap.putAll(localMultiMap);
                        }
                    }
                }
            }
            return globalMultiMap;
        } else {
            throw new MetricNotFoundException("The base metric" + MetricPathUtils.getMetricName(baseMetricExpression) + "does not exist in the baseMetricsMap");
        }
    }

    private SetMultimap<String, String> splitAndPopulateVariablesMap(List<String> baseMetricExpressionList, List<String> baseMetricPathList) {
        SetMultimap<String, String> localMultiMap = HashMultimap.create();
        Iterator expressionIterator = baseMetricExpressionList.iterator();
        Iterator pathIterator = baseMetricPathList.iterator();
        while (expressionIterator.hasNext() && pathIterator.hasNext()) {
            String expressionValue = expressionIterator.next().toString();
            String nameValue = pathIterator.next().toString();
            if (expressionValue.startsWith("{") && expressionValue.endsWith("}")) {
                localMultiMap.put(expressionValue, nameValue);
            } else if (!expressionValue.equals(nameValue)) {
                return null;
            }
        }
        return localMultiMap;
    }
}
