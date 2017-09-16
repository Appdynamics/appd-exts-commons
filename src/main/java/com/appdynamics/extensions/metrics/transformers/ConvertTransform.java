package com.appdynamics.extensions.metrics.transformers;

import com.appdynamics.extensions.metrics.Metric;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by venkata.konala on 8/31/17.
 */
class ConvertTransform {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConvertTransform.class);

    void convert(Metric metric){
        Map<Object, Object> convertMap = metric.getMetricProperties().getConversionValues();
        String metricValue = metric.getMetricValue();
        if(convertMap != null &&  !convertMap.isEmpty() && convertMap.containsKey(metricValue)){
            metric.setMetricValue(convertMap.get(metricValue).toString());
            logger.debug("Applied conversion on {} and replaced value {} with {}", metric.getMetricPath(), metricValue, metric.getMetricValue());
        }
    }
}
