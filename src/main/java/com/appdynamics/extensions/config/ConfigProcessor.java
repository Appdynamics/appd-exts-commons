/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.config;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Satish Muddam
 */

/**
 * This class processes config file and substitutes the environment variables defined in config file with values.<br>
 * Usage:
 * <p>
 * In config file, define any property you want to pass through environment variables as ${ENV_VAR_NAME}
 * <pre>
 * servers:
 *   - name: "${TEST_SERVER1}"
 *     host: "${TEST_HOST1}"
 * </pre>
 * and set these environment variables in system environment. This class will replace the variables in config.yml
 * with the values defined in system environment.
 */
public class ConfigProcessor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ConfigProcessor.class);

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    private static final Map<String, String> SYSTEM_ENV_VARS = System.getenv();

    public static Map<String, ?> process(Map config) {
        // #TODO If config is null, it will never reach this point.
        // 1. There is already a null check happening in the MonitorContextConfiguration.
        // 2. Before maps are passed recursively in this method, it is checked if it is an "instanceof" Map, which would
        //    be false if the map is null.
        if (config == null) {
            logger.error("Empty configuration passed, ignoring");
            //#TODO Incase the config==null check needs to be retrieved, then there should be a return statement here,
            // otherwise it has a potential for NPE.
        }
        Set<String> keys = config.keySet();
        for (String key : keys) {
            Object value = config.get(key);
            if (value instanceof Map) {
                process((Map<String, Object>) value);
            } else if (value instanceof List) {
                for (Object curValue : (List) value) {
                    if (curValue instanceof Map) {
                        process((Map<String, Object>) curValue);
                    }
                }
            } else {
                if (value instanceof String) {
                    String thisValue = (String) value;
                    Matcher matcher = ENV_VAR_PATTERN.matcher(thisValue);
                    boolean found = matcher.find();
                    if (found) {
                        String systemVariableName = matcher.group(1);
                        String valueFromSystemEnv = SYSTEM_ENV_VARS.get(systemVariableName);
                        if (valueFromSystemEnv != null) {
                            config.put(key, valueFromSystemEnv);
                        } else {
                            logger.error("Could not find any system variable with name {}", systemVariableName);
                        }
                    }
                }
            }
        }
        return config;
    }
}