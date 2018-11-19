/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.eventsservice.utils;

import java.util.Map;

/**
 * @author : Aditya Jagtiani
 */
public class EventsServiceUtils {

    public static boolean isValid(Map<String, ?> eventsServiceParameters) {
        return eventsServiceParameters.get("host") != null
                && eventsServiceParameters.get("port") != null
                && eventsServiceParameters.get("globalAccount") != null
                && eventsServiceParameters.get("eventsApiKey") != null
                && eventsServiceParameters.get("useSsl") != null;
    }
}