package com.appdynamics.extensions.util;
/**
 * Created by venkata.konala on 8/13/17.
 */
import java.util.Map;

public class MetricPropertiesBuilder {
    private Map<String, ?> metricProperties;
    //private String metricPath;
    private String metricName;
    private String metricValue;

    public MetricPropertiesBuilder(Map<String, ?> metricProperties,String metricName, String metricValue){
        this.metricProperties = metricProperties;
        //this.metricPath = metricPath;
        this.metricName = metricName;
        this.metricValue = metricValue;
    }

    public MetricProperties buildMetricProperties(){
        MetricProperties metricProperties = new MetricProperties();
        //metricProperties.setMetricPath(metricPath);
        metricProperties.setMetricName(metricName);
        metricProperties.setMetricValue(metricValue);
        metricProperties.setAlias(this.metricProperties.get("alias").toString(), metricName);
        metricProperties.setMultiplier(this.metricProperties.get("multiplier").toString());
        metricProperties.setAggregationType(this.metricProperties.get("aggregation").toString());
        metricProperties.setTimeRollUp(this.metricProperties.get("timeRollUp").toString());
        metricProperties.setClusterRollUp(this.metricProperties.get("clusterRollUp").toString());
        metricProperties.setDelta(this.metricProperties.get("delta").toString());
        metricProperties.setConversionValues((Map<Object, Object>)this.metricProperties.get("cluster"));
        metricProperties.setAggregateAtCluster(this.metricProperties.get("aggregateAtCluster").toString());
        return metricProperties;
    }
}

