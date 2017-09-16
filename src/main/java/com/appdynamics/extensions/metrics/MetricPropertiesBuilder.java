package com.appdynamics.extensions.metrics;
/**
 * Created by venkata.konala on 8/13/17.
 */
import java.util.Map;

public class MetricPropertiesBuilder {
    private Map<String, ?> metricPropertiesFromConfig;
    private String metricName;

    public MetricPropertiesBuilder(Map<String, ?> metricPropertiesFromConfig,String metricName){
        this.metricPropertiesFromConfig = metricPropertiesFromConfig;
        this.metricName = metricName;
    }

    public MetricProperties buildMetricProperties(){
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setAlias(this.metricPropertiesFromConfig.get("alias") == null ? null : this.metricPropertiesFromConfig.get("alias").toString(), metricName);
        metricProperties.setMultiplier(this.metricPropertiesFromConfig.get("multiplier") == null ? null : this.metricPropertiesFromConfig.get("multiplier").toString());
        metricProperties.setAggregationType(this.metricPropertiesFromConfig.get("aggregationType") == null ? null : this.metricPropertiesFromConfig.get("aggregationType").toString());
        metricProperties.setTimeRollUpType(this.metricPropertiesFromConfig.get("timeRollUpType") == null ? null : this.metricPropertiesFromConfig.get("timeRollUpType").toString());
        metricProperties.setClusterRollUpType(this.metricPropertiesFromConfig.get("clusterRollUpType") == null ? null : this.metricPropertiesFromConfig.get("clusterRollUpType").toString());
        metricProperties.setDelta(this.metricPropertiesFromConfig.get("delta") == null ? null : this.metricPropertiesFromConfig.get("delta").toString());
        metricProperties.setConversionValues((Map<Object, Object>)this.metricPropertiesFromConfig.get("convert"));
        return metricProperties;
    }
}

