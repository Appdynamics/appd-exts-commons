/*
 * Copyright (c) 2019 AppDynamics,Inc.
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
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceModule.class);
    private EventsServiceDataManager eventsServiceDataManager;

    public void initEventsServiceDataManager(String monitorName, Map<String, ?> config) {
        if (config.containsKey("eventsServiceParameters")) {
            Map<String, ? super Object> eventsServiceParameters = (Map) config.get("eventsServiceParameters");
            if (isValid(eventsServiceParameters)) {
                addConnectionPropertiesToEventsServiceParameters(config, eventsServiceParameters);
                LOGGER.info("Events Service parameters validated successfully for monitor: {}. Initializing..", monitorName);
                eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParameters);
            } else {
                LOGGER.error("Events Service parameters invalid for monitor: {}. Check your config.yml and retry before " +
                        "proceeding", monitorName);
            }
        } else {
            LOGGER.info("Events Service parameters not set for monitor: {}. Skipping", monitorName);
        }
    }

    private void addConnectionPropertiesToEventsServiceParameters(Map<String, ?> config,
                                                                  Map<String, ? super Object> eventsServiceParameters) {
        if(config.containsKey("connection")) {
            eventsServiceParameters.put("connection", (Map)config.get("connection"));
        }
        if(config.containsKey("proxy")) {
            eventsServiceParameters.put("proxy", (Map)config.get("proxy"));
        }
    }

    public EventsServiceDataManager getEventsServiceDataManager() {
        if (eventsServiceDataManager != null) {
            return eventsServiceDataManager;
        }
        throw new RuntimeException("The Events Service Data Manager is not initialized.");
    }
}