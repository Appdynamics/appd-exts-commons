package com.appdynamics.extensions.util.derived;

import java.util.List;

/**
 * Created by venkata.konala on 8/28/17.
 */
public class MetricNameFetcher {
    private Splitters splitters = new Splitters();

    public String getMetricName(String metricPath){
        if(metricPath != null) {
            List<String> splitList = splitters.getPipeSplitter().splitToList(metricPath);
            if (splitList.size() > 0) {
                return splitList.get(splitList.size() - 1);
            }
        }
        return null;
    }
}
