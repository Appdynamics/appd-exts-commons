package com.appdynamics.extensions;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumberUtils {
    public static boolean isNumber(String str) {
        if (str != null) {
            str = str.trim();
            try {
                Double.parseDouble(str);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }
}
