package com.appdynamics.extensions.metrics.transformers;

import com.appdynamics.extensions.metrics.DeltaMetricsCalculator;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.NumberUtils;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/31/17.
 */
class DeltaTranform {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DeltaTranform.class);
    private static DeltaMetricsCalculator deltaCalculator = new DeltaMetricsCalculator(10);

    void applyDelta(Metric metric){
        String metricValue = metric.getMetricValue();
        if(NumberUtils.isNumber(metricValue) && metric.getMetricProperties().getDelta() == true){
            BigDecimal deltaValue = deltaCalculator.calculateDelta(metric.getMetricPath(), new BigDecimal(metricValue));
            if(deltaValue != null) {
                metric.setMetricValue(deltaValue.toString());
            }
            else{
                metric.setMetricValue(null);
            }
        }
    }
}
