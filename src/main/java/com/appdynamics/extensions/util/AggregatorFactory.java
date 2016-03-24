package com.appdynamics.extensions.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by abey.tom on 3/17/16.
 */
public class AggregatorFactory {
    private Map<String, Aggregator<AggregatorKey>> map = new HashMap<String, Aggregator<AggregatorKey>>();

    public Aggregator<AggregatorKey> getAggregator(String metricType) {
        String key = metricType.split("\\.")[2];
        Aggregator<AggregatorKey> aggregator = map.get(key);
        if (aggregator == null) {
            AggregationType aggType = key.equals("IND") ? AggregationType.AVERAGE : AggregationType.SUM;
            aggregator = new Aggregator<AggregatorKey>(aggType);
            map.put(key, aggregator);
        }
        return aggregator;
    }

    public Collection<Aggregator<AggregatorKey>> getAggregators(){
        return map.values();

    }

}
