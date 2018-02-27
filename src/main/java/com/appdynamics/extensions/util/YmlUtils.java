/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

import com.google.common.base.Strings;

import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 3/14/16.
 */
public class YmlUtils {

    public static Integer getInteger(Object numObj) {
        Integer in = null;
        if (numObj instanceof String) {
            String str = (String) numObj;
            //We want to fail if it is an invalid number
            if (!Strings.isNullOrEmpty(str)) {
                in = Integer.parseInt(str);
            }
        } else if (numObj instanceof Number) {
            in = ((Number) numObj).intValue();
        }
        if (numObj == null) {

        }
        return in;
    }

    public static Boolean getBoolean(Object bool) {
        if (bool instanceof Boolean) {
            return (Boolean) bool;
        } else if (bool instanceof String) {
            String boolStr = (String) bool;
            if (!Strings.isNullOrEmpty(boolStr)) {
                return Boolean.parseBoolean(boolStr);
            }
        }
        return null;
    }

    public static String[] asStringArray(Object value) {
        if (value instanceof List) {
            List<String> values = (List) value;
            if (!values.isEmpty()) {
                return values.toArray(new String[values.size()]);
            }
        } else if (value instanceof String) {
            String val = (String) value;
            if (!Strings.isNullOrEmpty(val)) {
                return val.trim().split(",");
            }
        }
        return null;
    }

    //iteratively get a nested object from the YML Map
    public static Object getNestedObject(Map map, String... keys) {
        if (map != null && keys != null) {
            Map parent = map;
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                Object o = parent.get(key);
                if (o instanceof Map) {
                    parent = (Map) o;
                } else if (i == keys.length - 1) {
                    return o;
                } else {
                    return null;
                }
            }
            return parent;
        }
        return null;
    }

    public static int getInt(Object value, int defaultVal) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else{
            return defaultVal;
        }
    }
    public static long getLong(Object value, long defaultVal) {
        if (value instanceof Long) {
            return (Long) value;
        } else{
            return defaultVal;
        }
    }
}
