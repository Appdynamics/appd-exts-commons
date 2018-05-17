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

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/1/14
 * Time: 9:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class Aggregator<K> {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(Aggregator.class);
    private AggregationType aggregationType;
    public Map<K, AggregatedValue> map;

    public Aggregator() {
        this(AggregationType.SUM);
    }

    public Aggregator(AggregationType aggregationType) {
        if (aggregationType != null) {
            this.aggregationType = aggregationType;
        } else {
            this.aggregationType = AggregationType.SUM;
        }
    }

    public void add(K key, String value) {
        BigDecimal val = toBigDecimal(value);
        add(key, val);
    }

    public void add(K key, BigDecimal val) {
        if (map == null) {
            map = Maps.newHashMap();
        }
        AggregatedValue aggVal = map.get(key);
        if (aggVal == null) {
            aggVal = new AggregatedValue();
            map.put(key, aggVal);
        }
        aggVal.add(val);
    }

    public AggregatedValue get(K key) {
        if (map != null) {
            return map.get(key);
        }
        return null;
    }

    public BigDecimal getAggregatedValue(K key) {
        AggregatedValue aggVal = get(key);
        if (aggVal != null) {
            if (aggregationType.equals(AggregationType.SUM)) {
                return aggVal.getSum();
            } else {
                return aggVal.getAverage();
            }
        }
        return null;
    }

    public Set<K> keys() {
        if (map != null) {
            return map.keySet();
        }
        return null;
    }

    private BigDecimal toBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            logger.error("Error while parsing the value " + value + " to BigDecimal, returning zero", e);
            return new BigDecimal(0);
        }
    }

    public boolean isEmpty() {
        return map == null || map.isEmpty();
    }

}

