package com.appdynamics.extensions.util.derived;

import java.util.List;
import static com.appdynamics.extensions.util.derived.Constants.pipeSplitter;

/**
 * Created by venkata.konala on 8/28/17.
 */
class MetricNameFetcher {

     String getMetricName(String metricPath){
        if(metricPath != null) {
            List<String> splitList = pipeSplitter.splitToList(metricPath);
            if (splitList.size() > 0) {
                return splitList.get(splitList.size() - 1);
            }
        }
        return null;
    }
}
