package com.appdynamics.extensions.util;
/**
 * Created by venkata.konala on 8/13/17.
 */

import com.appdynamics.extensions.NumberUtils;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import java.math.BigDecimal;
import java.util.Map;

public class MetricProperties {
    private String alias;
    private BigDecimal multiplier;
    private String aggregationType;
    private String timeRollUpType;
    private String clusterRollUpType;
    private boolean delta;
    private Map<Object, Object> conversionValues;

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

    public String getTimeRollUpType(){
        return timeRollUpType;
    }

    public void setTimeRollUpType(String timeRollUpType){
        if(timeRollUpType != null  && (timeRollUpType.equals("AVERAGE") || timeRollUpType.equals("SUM") || timeRollUpType.equals("CURRENT"))){
            this.timeRollUpType = timeRollUpType;
        }
        else{
            this.timeRollUpType = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;
        }
    }

    public String getClusterRollUpType(){
        return clusterRollUpType;
    }

    public void setClusterRollUpType(String clusterRollUpType){
        if(clusterRollUpType != null  && (clusterRollUpType.equals("INDIVIDUAL") || clusterRollUpType.equals("COLLECTIVE"))){
            this.clusterRollUpType = clusterRollUpType;
        }
        else{
            this.clusterRollUpType = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
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
}

