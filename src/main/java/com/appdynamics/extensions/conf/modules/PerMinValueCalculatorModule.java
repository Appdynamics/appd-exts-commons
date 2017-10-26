package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.metrics.PerMinValueCalculator;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class PerMinValueCalculatorModule {

    private PerMinValueCalculator perMinValueCalculator;

    public PerMinValueCalculator getPerMinValueCalculator() {
        if (perMinValueCalculator == null) {
            perMinValueCalculator = new PerMinValueCalculator();
        }
        return perMinValueCalculator;
    }
}
