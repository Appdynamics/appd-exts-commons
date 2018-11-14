/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

import static com.appdynamics.extensions.eventsservice.utils.EventsServiceUtils.isValid;

/**
 * @author : Aditya Jagtiani
 */
public class EventsServiceModule {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(DerivedMetricsModule.class);

    public EventsServiceDataManager initEventsServiceDataManager(String monitorName, Map<String, ?> config) {
        if (config.containsKey("eventsServiceParameters")) {
                    Map<String, ?> eventsServiceParameters = (Map) config.get("eventsServiceParameters");
                    if (isValid(eventsServiceParameters)) {
                        LOGGER.info("Events Service parameters validated successfully for monitor: {}. Initializing..",
                                monitorName);
                return new EventsServiceDataManager(eventsServiceParameters);
            } else {
                LOGGER.error("Events Service parameters invalid for monitor: {}. Check your config.yml and retry before " +
                        "proceeding", monitorName);
            }
        } else {
            LOGGER.info("Events Service parameters not set for monitor: {}. Skipping", monitorName);
        }
        return null;
    }
}