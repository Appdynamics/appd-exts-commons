package com.appdynamics.extensions.util;

import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.List;

import static com.appdynamics.extensions.util.StringUtils.hasText;

/**
 * Created by venkata.konala on 1/7/19.
 */
public class ValidationUtils {

    private static final Splitter COLON_SPLITTER = Splitter.on(":").trimResults().omitEmptyStrings();
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ValidationUtils.class);

    // #TODO Move it out of here to a separate Validation class.
    // #TODO If SIM is enabled, then use Custom Metrics. Enforce this.
    public static boolean isValidMetric(String metricPath, String metricValue,
                                        String aggregationType, String timeRollup, String clusterRollup) {
       return isValidString(metricPath, metricValue, timeRollup, clusterRollup) && isValidMetricValue(metricValue) &&
               isValidMetricPath(metricPath);
    }

    public static boolean isValidString(String... args) {
        if (args != null) {
            for (String arg : args) {
                if (Strings.isNullOrEmpty(arg)) {
                    logger.debug("The metric path, metric value or qualifiers cannot be null or empty.");
                    return false;
                }
            }
            return true;
        }
        logger.debug("The metric path, metric value or qualifiers cannot be null or empty.");
        return false;
    }

    public static boolean isValidMetricValue(String metricValue) {
        if (metricValue != null && NumberUtils.isNumber(metricValue) && !NumberUtils.isNegative(metricValue)) {
            return true;
        }
        logger.debug("The metric value {} should be a positive number.", metricValue);
        return false;
    }

    public static boolean isValidMetricPath(String metricPath) {
        if (!metricPath.contains(",") && !metricPath.contains("||") && !metricPath.endsWith("|") && CharMatcher
                .ascii().matchesAllOf(metricPath) && isValidMetricPrefix(metricPath)) {
            return true;
        }
        logger.debug("The metric path {} is invalid", metricPath);
        return false;
    }

    public static boolean isValidMetricPrefix(String metricPath) {
        if (metricPath.startsWith("Server|Component:")) {
            List<String> tokens = MetricPathUtils.PIPE_SPLITTER.splitToList(metricPath);
            if (tokens.size() > 3 && tokens.get(2).equals("Custom Metrics")) {
                List<String> component = COLON_SPLITTER.splitToList(tokens.get(1));
                if (component.size() > 1) {
                    String tier = component.get(1);
                    return hasText(tier) && !tier.startsWith("<") && !tier.endsWith(">");
                }
            }
            logger.debug("Metric prefix {} is invalid. The third token of prefix should be 'Custom Metrics' (case sensitive) " +
                    "Component should be configured", metricPath);
            return false;
        }
        if (metricPath.startsWith("Custom Metrics|")) {
            return true;
        }
        logger.debug("Metric prefix {} should either start with 'Server|Component:' or 'Custom Metrics|' (case sensitive).",
                metricPath);
        return false;
    }
}
