package com.appdynamics.extensions.util;
/**
 * Created by venkata.konala on 8/13/17.
 */
import java.math.BigDecimal;
import java.util.Map;

import com.appdynamics.extensions.NumberUtils;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.MetricWriter;

public class MetricProperties {
    private String metricName;
    private BigDecimal metricValue;
    private String alias;
    private BigDecimal multiplier;
    private String aggregationType;
    private String timeRollUp;
    private String clusterRollUp;
    private boolean delta;
    private Map<Object, Object> conversionValues;
    private boolean aggragateAtCluster;

    public String getMetricName(){
        return metricName;
    }

    public void setMetricName(String metricName){
        if(Strings.isNullOrEmpty(metricName)){
            this.metricName = null;
        }
        else{
            this.metricName = metricName;
        }
    }

    public BigDecimal getMetricValue(){
        return metricValue;
    }

    public void setMetricValue(String metricValue){
        if(Strings.isNullOrEmpty(metricValue)){
            this.metricValue = null;
        }
        else{
            this.metricValue = new BigDecimal(metricValue);
        }
    }

    public String getAlias(){
        return alias;
    }

    public void setAlias(String alias, String actualMetricName){
        if(Strings.isNullOrEmpty(alias)){
            this.alias = actualMetricName;
        }
        else{
            this.alias = alias;
        }
    }

    public BigDecimal getMultiplier(){
        return multiplier;
    }

    public void setMultiplier(String multiplier){
        BigDecimal multiplierBigD = NumberUtils.isNumber(multiplier) ? new BigDecimal(multiplier.trim()) : BigDecimal.ONE;
        this.multiplier = multiplierBigD;
    }

    public String getAggregationType(){
        return aggregationType;
    }

    public void setAggregationType(String aggregationType){
        if(aggregationType != null  && (aggregationType.equals("AVERAGE") || aggregationType.equals("SUM") || aggregationType.equals("OBSERVATION"))){
            this.aggregationType = aggregationType;
        }
        else{
            this.aggregationType = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
        }
    }

    public String getTimeRollUp(){
        return timeRollUp;
    }

    public void setTimeRollUp(String timeRollUp){
        if(timeRollUp != null  && (timeRollUp.equals("AVERAGE") || timeRollUp.equals("SUM") || timeRollUp.equals("CURRENT"))){
            this.timeRollUp = timeRollUp;
        }
        else{
            this.timeRollUp = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;
        }
    }

    public String getClusterRollUp(){
        return clusterRollUp;
    }

    public void setClusterRollUp(String clusterRollUp){
        if(clusterRollUp != null  && (clusterRollUp.equals("INDIVIDUAL") || clusterRollUp.equals("COLLECTIVE"))){
            this.clusterRollUp = clusterRollUp;
        }
        else{
            this.clusterRollUp = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        }
    }

    public boolean getDelta() {
        return delta;
    }

    public void setDelta(String delta){
        if(Strings.isNullOrEmpty(delta) || !delta.equalsIgnoreCase("true")){
            this.delta = false;
        }
        else{
            this.delta = true;
        }
    }

    public Map<Object, Object> getConversionValues(){
        return conversionValues;
    }

    public void setConversionValues(Map<Object, Object> conversionValues){
        if(conversionValues == null){
            this.conversionValues = null;
        }
        else{
            this.conversionValues = conversionValues;
        }
    }

    public boolean getAggregateAtCluster(){
        return this.aggragateAtCluster;
    }

    public void setAggregateAtCluster(String aggregateAtCluster){
        if(Strings.isNullOrEmpty(aggregateAtCluster) || !aggregateAtCluster.equalsIgnoreCase("true")){
            this.aggragateAtCluster = false;
        }
        else{
            this.aggragateAtCluster = true;
        }
    }
}

