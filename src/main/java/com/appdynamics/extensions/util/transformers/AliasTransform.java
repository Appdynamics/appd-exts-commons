package com.appdynamics.extensions.util.transformers;

import com.appdynamics.extensions.util.Metric;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by venkata.konala on 9/7/17.
 */
class AliasTransform {
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
                metric.setMetricPath(Joiner.on("|").join(splitList));
            }
        }
    }

}
