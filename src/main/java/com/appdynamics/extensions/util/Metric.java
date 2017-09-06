package com.appdynamics.extensions.util;

import java.util.Map;

/**
 * Created by venkata.konala on 8/31/17.
 */
public class Metric {
    private String metricName;
    private String metricValue;
    private String metricPath;
    private MetricProperties metricProperties;

    public Metric(String metricName,String metricValue,String metricPath){
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricPath = metricPath;
        this.metricProperties = new DefaultMetricProperties(metricName);
    }

    public Metric(String metricName, String metricValue, String metricPath, Map<String, ?> metricProperties){
        this(metricName,metricValue,metricPath);
        AssertUtils.assertNotNull(metricProperties,"Metric Properties cannot be null");
        this.metricProperties = buildMetricProperties(metricProperties);
    }

    private MetricProperties buildMetricProperties(Map<String, ?> metricProperties){
        MetricPropertiesBuilder metricPropertiesBuilder = new MetricPropertiesBuilder(metricProperties, metricName);
        return metricPropertiesBuilder.buildMetricProperties();
    }

    public String getMetricName(){
        return metricName;
    }

    public String getMetricValue(){
        return metricValue;
    }

    public void setMetricValue(String metricValue){
        this.metricValue = metricValue;
    }

    public String getMetricPath(){
        return metricPath;
    }

    public MetricProperties getMetricProperties(){
        return metricProperties;
    }
}
