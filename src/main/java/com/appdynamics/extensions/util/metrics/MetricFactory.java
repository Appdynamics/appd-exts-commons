package com.appdynamics.extensions.util.metrics;


import com.appdynamics.extensions.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class MetricFactory<T> {

    protected RegexMap<MetricOverride> regexMap = new RegexMap<MetricOverride>();
    public static final Logger logger = Logger.getLogger(MetricFactory.class);

    public MetricFactory(MetricOverride[] metricOverrides){
        if(metricOverrides != null){
            for(MetricOverride override : metricOverrides) {
                if (StringUtils.hasText(override.getMetricKey())) {
                    regexMap.put(override.getMetricKey(), override);
                }
            }
        }
    }

    public List<Metric> process(Map<String,T> metricsMap){
        List<Metric> allMetrics = new ArrayList<Metric>();
        if(metricsMap != null) {
            for (Map.Entry<String, T> entry : metricsMap.entrySet()) {
                String metricKey = entry.getKey();
                Object metricValue = entry.getValue();
                Metric metric = getMetric(metricKey, metricValue);
                if (metric != null && !metric.isDisabled()) {
                    allMetrics.add(metric);
                } else {
                    logger.warn("Ignoring metric with metricKey= " + metricKey + " ,metricValue= " + metricValue);
                }
            }
        }
        return allMetrics;
    }

    public Metric getMetric(String metricKey,Object metricValue) {
        if(isValid(metricKey,metricValue)){
            MetricOverride override = regexMap.get(metricKey);
            MetricProperties metricProperties = getMetricProperties(override);
            return new Metric(metricKey,metricValue,metricProperties);
        }
        return null;
    }

    private boolean isValid(String metricKey, Object metricValue) {
        if(metricKey != null && !metricKey.equalsIgnoreCase("") && isMetricValueValid(metricValue)){
            return true;
        }
        return false;
    }

    private boolean isMetricValueValid(Object metricValue) {
        if(metricValue == null){
            return false;
        }
        if(metricValue instanceof String){
            try {
                Double.valueOf((String) metricValue);
                return true;
            }
            catch(NumberFormatException nfe){
                logger.warn("Metric Value is invalid" + nfe);
            }
        }
        else if(metricValue instanceof Number){
            return true;
        }
        return false;
    }

    private MetricProperties getMetricProperties(MetricOverride override) {
        if(override == null){
            return new MetricProperties();
        }
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setDisabled(override.isDisabled());
        metricProperties.setMetricPrefix(override.getPrefix());
        metricProperties.setAggregator(override.getAggregator());
        metricProperties.setClusterRollup(override.getClusterRollup());
        metricProperties.setTimeRollup(override.getTimeRollup());
        metricProperties.setMetricPostfix(override.getPostfix());
        metricProperties.setMultiplier(override.getMultiplier());
        return metricProperties;
    }
}
