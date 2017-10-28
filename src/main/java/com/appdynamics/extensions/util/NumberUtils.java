package com.appdynamics.extensions.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumberUtils {
    public static boolean isNumber(String str) {
        if (str != null && !str.equalsIgnoreCase("nan")) {
            str = str.trim();
            try {
                Double.parseDouble(str);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    /*
      Please make sure String str is a valid number before passing it to this method.
     */
    public static boolean isNegative(String str){
        if(Double.parseDouble(str) < 0){
            return true;
        }
        return false;
    }

    public static String roundToWhole(BigDecimal value){
        return value.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString();
    }
}
