package com.appdynamics.extensions.metrics.transformers;

import com.appdynamics.extensions.util.NumberUtils;
import com.appdynamics.extensions.metrics.Metric;

import java.math.BigDecimal;

/**
 * Created by venkata.konala on 8/31/17.
 */
class MultiplierTransform {

     void multiply(Metric metric){
         String metricValue = metric.getMetricValue();
         if(NumberUtils.isNumber(metricValue)) {
             BigDecimal metricValueBigD = new BigDecimal(metricValue);
             metric.setMetricValue((metricValueBigD.multiply(metric.getMetricProperties().getMultiplier())).toString());
         }
    }
}
