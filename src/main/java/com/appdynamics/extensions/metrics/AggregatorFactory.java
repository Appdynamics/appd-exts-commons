/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.metrics;

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
