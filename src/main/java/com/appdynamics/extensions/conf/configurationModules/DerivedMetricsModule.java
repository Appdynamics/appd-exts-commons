package com.appdynamics.extensions.conf.configurationModules;

import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class DerivedMetricsModule {

    private static final Logger logger = LoggerFactory.getLogger(DerivedMetricsModule.class);

    public DerivedMetricsCalculator initDerivedMetricsCalculator(Map<String, ?> config, String metricPrefix) {
        List<Map<String, ?>> derivedMetricsList = (List) config.get("derivedMetrics");
        if(derivedMetricsList !=  null){
            logger.info("The DerivedMetricsCalculator is initialized");
            return new DerivedMetricsCalculator(derivedMetricsList, metricPrefix);
        }
        else{
            logger.info("The DerivedMetricsCalculator is not initialized.");
        }
        return null;
    }
}
