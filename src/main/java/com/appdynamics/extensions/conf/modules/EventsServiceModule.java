package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author: Aditya Jagtiani
 */
public class EventsServiceModule {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(DerivedMetricsModule.class);

    public EventsServiceDataManager initEventsServiceDataManager(String monitorName, Map<String, ?> config) {
        if(config.containsKey("eventsServiceParameters")) {
            Map<String, ?> eventsServiceParameters = (Map) config.get("eventsServiceParameters");
            if(isValid(eventsServiceParameters)) {
                LOGGER.info("Events Service parameters validated successfully for monitor: {}. Initializing..", monitorName);
                return new EventsServiceDataManager(monitorName, eventsServiceParameters);
            }
            else {
                LOGGER.error("Events Service parameters invalid for monitor: {}. Please check your config.yml", monitorName);
            }
        }
        else {
            LOGGER.info("Events Service parameters not set for monitor: {}. Skipping", monitorName);
        }
        return null;
    }

    private boolean isValid(Map<String, ?> eventsServiceParameters) {
        return (eventsServiceParameters.containsKey("host") && eventsServiceParameters.get("host") != null)
                && (eventsServiceParameters.containsKey("port") && eventsServiceParameters.get("port") != null)
                && (eventsServiceParameters.containsKey("globalAccountName") && eventsServiceParameters.get("globalAccount") != null)
                && (eventsServiceParameters.containsKey("eventsApiKey") && eventsServiceParameters.get("eventsApiKey") != null)
                && (eventsServiceParameters.containsKey("useSsl") && eventsServiceParameters.get("useSsl") != null);
    }
}