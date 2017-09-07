package com.appdynamics.extensions.util.derived;

import java.util.List;
import static com.appdynamics.extensions.util.derived.Splitters.PIPE_SPLITTER;

/**
 * Created by venkata.konala on 8/28/17.
 */
class DerivedMetricsPathHandler {

    String getMetricName(String metricPath){
        if(metricPath != null) {
            List<String> splitList = PIPE_SPLITTER.splitToList(metricPath);
            if (splitList.size() > 0) {
                return splitList.get(splitList.size() - 1);
            }
        }
        return null;
    }

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
