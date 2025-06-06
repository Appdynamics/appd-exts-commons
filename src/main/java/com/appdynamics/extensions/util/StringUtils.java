/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/1/14
 * Time: 7:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {

    public static final int INDEX_NOT_FOUND = -1;
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(StringUtils.class);

    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Removes the leading occurence of the string trim.
     * <p/>
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
     * <p/>
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
     * <p/>
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

    public static String trim(String str, String... trims) {
        for(String trim : trims) {
            str = trimLeading(str, trim);
            str = trimTrailing(str, trim);
        }
        return str;
    }

    public static String stripQuote(String str) {
        if (str != null) {
            return str.replaceAll("\"", "");
        }
        return str;
    }


    public static int countMatches(String str, String sub) {
        if (!hasText(str) || !hasText(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != INDEX_NOT_FOUND) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    public static List<Integer> indicesOf(String str, String sub) {
        if (!hasText(str) || !hasText(sub)) {
            return Collections.emptyList();
        }
        List<Integer> list = new ArrayList<Integer>();
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != INDEX_NOT_FOUND) {
            list.add(idx);
            idx += sub.length();
        }
        return list;
    }


    public static String unescapeXml(String str) {
        if (str != null) {
            return str.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&apos;",
                    "'").replaceAll("&quot;", "\"");
        }
        return str;
    }

    public static String concatMetricPath(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (StringUtils.hasText(path)) {
                String trimmed = trim(path.trim(), "|").trim();
                if (trimmed.length() > 0) {
                    sb.append(trimmed).append("|");
                }
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
