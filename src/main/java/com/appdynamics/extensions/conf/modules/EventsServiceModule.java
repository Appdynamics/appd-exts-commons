package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Author: Aditya Jagtiani
 */
public class EventsServiceModule {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(DerivedMetricsModule.class);

    public EventsServiceDataManager initEventsServiceDataManager(String monitorName, Map<String, ?> config) {
        if(config.get("sendDataToEventsService").equals(true)) {
            LOGGER.info("Initializing the Events Service Data Manager");
            Map<String, ?> eventsServiceParameters =  (Map)config.get("eventsServiceParameters");
            return new EventsServiceDataManager(monitorName, eventsServiceParameters);
        }
        LOGGER.info("The Events Service Data Manager is not initialized");
        return null;
    }
}