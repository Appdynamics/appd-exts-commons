/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.eventsservice.utils;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author : Aditya Jagtiani
 */
public class EventsServiceUtils {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceUtils.class);

    public static void closeHttpResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ex) {
                LOGGER.error("Error encountered while closing the HTTP response", ex);
            }
        }
    }

    public static boolean isValid(Map<String, ?> eventsServiceParameters) {
        return (eventsServiceParameters.containsKey("host") && eventsServiceParameters.get("host") != null)
                && (eventsServiceParameters.containsKey("port") && eventsServiceParameters.get("port") != null)
                && (eventsServiceParameters.containsKey("globalAccountName") && eventsServiceParameters.get("globalAccount") != null)
                && (eventsServiceParameters.containsKey("eventsApiKey") && eventsServiceParameters.get("eventsApiKey") != null)
                && (eventsServiceParameters.containsKey("useSsl") && eventsServiceParameters.get("useSsl") != null);
    }
}