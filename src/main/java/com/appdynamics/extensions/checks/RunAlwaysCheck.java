/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

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
