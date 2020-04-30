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

package com.appdynamics.extensions.metrics.transformers;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by venkata.konala on 8/31/17.
 */
public class Transformer {
    private static Logger logger = ExtensionsLoggerFactory.getLogger(Transformer.class);
    private List<Metric> metricList;
    private DeltaTranform deltaTranform = new DeltaTranform();
    private MultiplierTransform multiplierTransform = new MultiplierTransform();
    private ConvertTransform convertTransform = new ConvertTransform();
    private AliasTransform aliasTransform = new AliasTransform();

    public Transformer(List<Metric> metricList) {
        AssertUtils.assertNotNull(metricList, "Metrics List cannot be null");
        this.metricList = metricList;
    }


    public void transform() {
        for (Metric metric : metricList) {
            applyTransforms(metric);
        }
    }

    private void applyTransforms(Metric metric) {
        aliasTransform.applyAlias(metric);
        if (metric.getMetricValue() != null) {
            convertTransform.convert(metric);
        }
        if (metric.getMetricValue() != null) {
            deltaTranform.applyDelta(metric);
        }
        if (metric.getMetricValue() != null) {
            multiplierTransform.multiply(metric);
        }
        if (metric.getMetricValue() != null) {
            try {
                BigDecimal value = new BigDecimal(metric.getMetricValue());
                metric.setMetricValue(value.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString());
            } catch (NumberFormatException e) {
                logger.debug(e.toString());
            }

        }
    }
}
