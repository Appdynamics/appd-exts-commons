package com.appdynamics.extensions.util;

import java.math.BigDecimal;

/**
 * Created by kunal.gupta on 7/22/14.
 */
public class MetricUtils {

    public static final String ONE = "1";

    /**
     * Currently, appD controller only supports Integer values. This function will round all the decimals into integers and convert them into strings.
     * If number is less than 0.5, Math.round will round it to 0 which is not useful on the controller.
     *
     * @param attribute
     * @return
     */
    public static String toWholeNumberString(Object attribute) {
        if (attribute instanceof Double) {
            Double d = (Double) attribute;
            if (d > 0 && d < 1.0d) {
                return ONE;
            }
            return String.valueOf(Math.round(d));
        } else if (attribute instanceof Float) {
            Float f = (Float) attribute;
            if (f > 0 && f < 1.0f) {
                return ONE;
            }
            return String.valueOf(Math.round((Float) attribute));
        }
        return attribute.toString();
    }

    public static BigDecimal multiplyAndRound(String value, BigDecimal multiplier) {
        BigDecimal newValue;
        if (multiplier != null) {
            newValue = new BigDecimal(value).multiply(multiplier);
        } else {
            newValue = new BigDecimal(value);
        }
        return newValue.setScale(0, BigDecimal.ROUND_HALF_UP);
    }


}
