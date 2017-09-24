package com.appdynamics.extensions.util;


import com.google.common.base.Splitter;

import java.util.List;

public class MetricPathUtils {

    public static final Splitter PIPE_SPLITTER = Splitter.on('|')
            .omitEmptyStrings()
            .trimResults();

    public static String getMetricName(String metricPath){
        if(metricPath != null) {
            List<String> splitList = PIPE_SPLITTER.splitToList(metricPath);
            if (splitList.size() > 0) {
                return splitList.get(splitList.size() - 1);
            }
        }
        return null;
    }
}
