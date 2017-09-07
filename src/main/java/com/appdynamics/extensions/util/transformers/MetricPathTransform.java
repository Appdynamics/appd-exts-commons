package com.appdynamics.extensions.util.transformers;

import com.appdynamics.extensions.util.Metric;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by venkata.konala on 9/7/17.
 */
class MetricPathTransform {
    Splitter PIPE_SPLITTER = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    void applyAlias(Metric metric){
        String metricName = metric.getMetricName();
        String alias = metric.getMetricProperties().getAlias();
        String metricPath = metric.getMetricPath();
        List<String> splitList = new ArrayList<>(PIPE_SPLITTER.splitToList(metricPath));
        if (splitList.size() > 0) {
            String metricNameFromSplit = splitList.get(splitList.size() - 1);
            if(metricNameFromSplit.equals(metricName)){
                splitList.remove(splitList.size() - 1);
                splitList.add(alias);
                metric.setMetricPath(convertListToString(splitList));
            }
        }
    }

    private String convertListToString(List<String> splitList){
        StringBuilder transformedMetricPath = new StringBuilder();
        int count = 1;
        int size = splitList.size();
        for(String component : splitList){
            if(count != size) {
                transformedMetricPath.append(component + "|");
            }
            else{
                transformedMetricPath.append(component);
            }
            count++;
        }
        return transformedMetricPath.toString();
    }
}
