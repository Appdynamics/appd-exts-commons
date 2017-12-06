package com.appdynamics.extensions.checks;

import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public interface RunAlwaysCheck extends Check {
    
    long getPeriod();

    TimeUnit getTimeUnit();

    boolean shouldStop();
}
