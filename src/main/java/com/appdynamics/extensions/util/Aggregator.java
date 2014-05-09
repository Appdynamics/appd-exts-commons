package com.appdynamics.extensions.util;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/1/14
 * Time: 9:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class Aggregator<K> {
    public static final Logger logger = LoggerFactory.getLogger(Aggregator.class);
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
        if (map == null) {
            map = Maps.newHashMap();
        }
        AggregatedValue aggVal = map.get(key);
        BigDecimal val = toBigDecimal(value);
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

