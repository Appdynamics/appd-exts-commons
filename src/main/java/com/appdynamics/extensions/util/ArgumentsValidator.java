package com.appdynamics.extensions.util;

import com.appdynamics.extensions.TaskInputArgs;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/16/14
 * Time: 6:34 PM
 * This class can be used to validate the arguments,
 * as of now, it validates on the metric prefix and default the garuments which are not available in the input.
 */
public class ArgumentsValidator {
    public static final Logger logger = LoggerFactory.getLogger(ArgumentsValidator.class);

    /**
     * Set the default value if the argument is not present in the input args.
     *
     * @param argsMap
     * @param defaultArgs
     */
    public static Map<String, String> validateArguments(Map<String, String> argsMap, Map<String, String> defaultArgs) {
        if (argsMap == null) {
            argsMap = Maps.newHashMap();
        }
        if (defaultArgs != null) {
            for (String defaultKey : defaultArgs.keySet()) {
                if (!argsMap.containsKey(defaultKey)) {
                    String value = defaultArgs.get(defaultKey);
                    logger.debug("Adding the default argument {} with value {}", defaultKey, value);
                    argsMap.put(defaultKey, value);
                }
            }
        } else {
            throw new IllegalArgumentException("The default argument map cannot be null");
        }
        validate(argsMap);
        return argsMap;
    }

    private static void validate(Map<String, String> argsMap) {
        String metricPrefix = argsMap.get(TaskInputArgs.METRIC_PREFIX);
        if (metricPrefix != null) {
            argsMap.put(TaskInputArgs.METRIC_PREFIX, trimTrailingPipe(metricPrefix.trim()));
        }
    }

    public static void assertNotEmpty(Map<String, String> argsMap, String... args) {
        for (String arg : args) {
            String value = argsMap.get(arg);
            if (!StringUtils.hasText(value)) {
                String message = "The value is not set for the argument '%s' in the input map %s";
                throw new InvalidInputException(String.format(message, arg, argsMap));
            }
        }
    }

    private static String trimTrailingPipe(String str) {
        while (str.endsWith("|")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static class InvalidInputException extends RuntimeException {
        public InvalidInputException(String message) {
            super(message);
        }
    }
}
