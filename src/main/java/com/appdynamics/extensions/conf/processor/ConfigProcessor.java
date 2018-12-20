/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf.processor;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
 *
 * @author Satish Muddam
 */
public class ConfigProcessor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ConfigProcessor.class);

    private static final String EXTENSIONS_CONFIG_PROP_FILE = "EXTENSIONS_CONFIG_FILE";

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");


    private static final Map<String, String> SYSTEM_ENV_VARS = System.getenv();

    private static Map<String, String> extensionConfigProperties = new HashMap<>();

    static {
        init();
    }

    private static void init() {

        String extensionsConfigFile = SYSTEM_ENV_VARS.get(EXTENSIONS_CONFIG_PROP_FILE);
        if (extensionsConfigFile != null) {
            logger.info("Extension config properties file provided and replacing placeholders in config file using [" + extensionsConfigFile + "]");

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(extensionsConfigFile));
            } catch (Exception e) {
                logger.error("Unable to load properties from the provided extension config properties file [" + extensionsConfigFile + "]", e);
                throw new IllegalArgumentException("Unable to load properties from the provided extension config properties file [" + extensionsConfigFile + "]", e);
            }

            extensionConfigProperties = Maps.fromProperties(properties);
        } else {
            logger.info("Extension config properties file for replacing placeholders not provided");
            extensionConfigProperties = SYSTEM_ENV_VARS;
        }
    }

    public static Map<String, ?> process(Map config) {
        if (config == null) {
            logger.error("Empty configuration passed, ignoring");
            return null;
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
                        String valueFromSystemEnv = extensionConfigProperties.get(systemVariableName);
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