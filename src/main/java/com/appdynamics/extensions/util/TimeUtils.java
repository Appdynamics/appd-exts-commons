package com.appdynamics.extensions.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;

/**
 * Created by venkata.konala on 9/6/18.
 */
public class TimeUtils {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(TimeUtils.class);

    public static String getFormattedTimestamp(Long timeInMilli, String pattern) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(timeInMilli);
        } catch (Exception e) {
            logger.error("The time " + timeInMilli + " cannot be formatted to the pattern " + pattern);
        }
        return null;
    }
}