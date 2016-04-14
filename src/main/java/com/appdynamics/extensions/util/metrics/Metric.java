package com.appdynamics.extensions.util.metrics;



import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Metric {


    private String metricKey;
    private Object metricValue;
    private MetricProperties metricProperties;

    public Metric(String metricKey, Object metricValue,MetricProperties metricProperties) {
        this.metricKey = metricKey;
        this.metricValue = metricValue;
        this.metricProperties = metricProperties;
    }

    public String getMetricPath(){
        StringBuffer metricPath = new StringBuffer();
        if(!isEmpty(metricProperties.getMetricPrefix())){
            metricPath.append(metricProperties.getMetricPrefix()).append(MetricConstants.METRICS_SEPARATOR);
        }
        metricPath.append(metricKey);
        if(!isEmpty(metricProperties.getMetricPostfix())){
            metricPath.append(metricProperties.getMetricPostfix());
        }
        return metricPath.toString();
    }

    public BigInteger getMetricValue(){
        try {
            BigDecimal bigD = new BigDecimal(metricValue.toString()).multiply(new BigDecimal(metricProperties.getMultiplier()));
            return bigD.setScale(0, RoundingMode.HALF_UP).toBigInteger();
        }
        catch(NumberFormatException nfe){
        }
        return BigInteger.ZERO;
    }


    public String getAggregator(){
        return metricProperties.getAggregator();
    }

    public String getTimeRollup(){
        return metricProperties.getTimeRollup();
    }

    public String getClusterRollup(){
        return metricProperties.getClusterRollup();
    }

    public boolean isDisabled(){
        return metricProperties.isDisabled();
    }

    private boolean isEmpty(String string) {
        return string.length() == 0;
    }
}
