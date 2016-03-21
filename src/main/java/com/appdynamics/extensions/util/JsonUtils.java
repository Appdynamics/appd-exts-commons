package com.appdynamics.extensions.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abey.tom on 3/16/16.
 */
public class JsonUtils {
    public static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static String asJson(Object object) {
        if (object != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                logger.error("Error while converting the Object to Json " + object, e);
            }
        }
        return null;
    }
}
