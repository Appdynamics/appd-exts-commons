package com.appdynamics.extensions.eventsservice.utils;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Author: Aditya Jagtiani
 */
public class EventsServiceUtils {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceUtils.class);

    public static boolean areEventsServiceParametersValid(Map<String, ?> eventsServiceParameters) {
        if (!eventsServiceParameters.containsKey("globalParameters") || !eventsServiceParameters.containsKey("httpParameters")
                || !eventsServiceParameters.containsKey("schemaParameters")) {
            LOGGER.error("Events Service parameters not configured correctly. Skipping upload to Events Service");
            return false;
        }
        return areEventServiceGlobalParametersValid((Map) eventsServiceParameters.get("globalParameters"))
                && areEventServiceHttpParametersValid((Map) eventsServiceParameters.get("httpParameters"))
                && areEventsServiceSchemaParametersValid((List) eventsServiceParameters.get("schemaParameters"));
    }

    private static boolean areEventServiceGlobalParametersValid(Map<String, String> globalParameters) {
        if ((!globalParameters.containsKey("globalAccountName") || !StringUtils.hasText(globalParameters.get("globalAccountName"))) ||
                (!globalParameters.containsKey("eventsApiKey") || !StringUtils.hasText(globalParameters.get("eventsApiKey")))) {
            return false;
        }
        for (Map.Entry<String, String> entry : globalParameters.entrySet()) {
            if (!StringUtils.hasText(entry.getValue())) {
                LOGGER.debug("Event Service Global Parameter: {} missing or invalid", entry.getKey());
                return false;
            }
        }
        return true;
    }

    private static boolean areEventServiceHttpParametersValid(Map<String, String> httpParameters) {
        if ((!httpParameters.containsKey("host") || !StringUtils.hasText(httpParameters.get("host"))) ||
                (!httpParameters.containsKey("port") || !StringUtils.hasText(httpParameters.get("port"))) ||
                !httpParameters.containsKey("sslEnabled") || !StringUtils.hasText(httpParameters.get("sslEnabled"))) {
            return false;
        }
        for (Map.Entry<String, String> entry : httpParameters.entrySet()) {
            if (!StringUtils.hasText(entry.getValue())) {
                LOGGER.debug("Event Service HTTP Parameter: {} missing or invalid", entry.getKey());
                return false;
            }
        }
        return true;
    }

    private static boolean areEventsServiceSchemaParametersValid(List<Map<String, String>> schemaParameters) {
        for (Map<String, String> schemaParameter : schemaParameters) {
            if ((!schemaParameter.containsKey("name") || !StringUtils.hasText(schemaParameter.get("name"))) ||
                    (!schemaParameter.containsKey("pathToSchemaJson") || !StringUtils.hasText(schemaParameter.get("pathToSchemaJson"))) ||
                    !schemaParameter.containsKey("recreateSchema") || !StringUtils.hasText(schemaParameter.get("recreateSchema"))) {
                return false;
            }

            for (Map.Entry<String, String> entry : schemaParameter.entrySet()) {
                if (!StringUtils.hasText(entry.getValue())) {
                    LOGGER.debug("Event Service Schema Parameter: {} missing or invalid", entry.getKey());
                    return false;
                }
            }
        }
        return true;
    }

    public static void closeHttpResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ex) {
                LOGGER.error("Error while closing the HTTP response", ex);
            }
        }
    }

}
