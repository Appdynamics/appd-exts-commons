package com.appdynamics.extensions.metrics.derived;

/**
 * Created by venkata.konala on 8/28/17.
 */
class DerivedMetricsPathHandler {


    StringBuilder getMetricPathWithMetricPrefix(String metricPath,String metricPrefix){
        StringBuilder derivedMetricPath = new StringBuilder(metricPrefix);
        derivedMetricPath.append(metricPath);
        return derivedMetricPath;
    }

    String getSubstitutedPath(String path, String variable, String variableValue){
        if(path.contains(variable)){
            path = path.replace(variable, variableValue);
        }
        return path;
    }
}
