package com.appdynamics.extensions;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/1/14
 * Time: 7:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {

    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Removes the leading occurence of the string trim.
     *
     * trim("||||FOO|BAR|||||","|") => FOO|BAR|||||
     *
     * @param str
     * @param trim
     * @return
     */
    public static String trimLeading(String str, String trim) {
        while (str.startsWith(trim)) {
            str = str.substring(trim.length());
        }
        return str;
    }

    /**
     * Removes the trailing occurence of the string trim.
     *
     * trim("||||FOO|BAR|||||","|") => ||||FOO|BAR
     *
     * @param str
     * @param trim
     * @return
     */
    public static String trimTrailing(String str, String trim) {
        while (str.endsWith(trim)) {
            str = str.substring(0, str.length() - trim.length());
        }
        return str;
    }

    /**
     * Removes the leading and trailing occurence of the string trim.
     *
     * trim("||||FOO|BAR|||||","|") => FOO|BAR
     *
     * @param str
     * @param trim
     * @return
     */
    public static String trim(String str, String trim) {
        str = trimLeading(str, trim);
        str = trimTrailing(str, trim);
        return str;
    }

    public static String stripQuote(String str){
        if(str != null){
            return str.replaceAll("\"","");
        }
        return str;
    }

}
