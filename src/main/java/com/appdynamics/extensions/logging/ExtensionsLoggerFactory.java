package com.appdynamics.extensions.logging;

import com.appdynamics.extensions.ExtensionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Satish Muddam
 */
public class ExtensionsLoggerFactory {

    public static Logger getLogger(String className) {

        String loggerName;

        if (ExtensionName.getName() != null) {
            loggerName = className + "-" + ExtensionName.getName();
        } else {
            loggerName = className;
        }
        return LoggerFactory.getLogger(loggerName);
    }

    public static Logger getLogger(Class className) {
        return getLogger(className.getName());
    }
}