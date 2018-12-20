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

package com.appdynamics.extensions;

import com.google.common.base.Strings;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/2/14
 */
public class Constants {
    public static final String URI = "uri";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String AUTHTYPE = "authType";
    public static final String USER = "username";
    public static final String PASSWORD = "password";
    public static final String ENCRYPTED_PASSWORD = "encryptedPassword";
    public static final String USE_SSL = "useSsl";
    public static final String ENCRYPTION_KEY = "encryptionKey";
    public static final String ENABLED = "enabled";
    // #TODO Check if the following can be removed.
    public static final String METRIC_PREFIX = "metric-prefix";
    public static final String AUTH_TYPE = "auth-type";

    public static String getArg(Map<String, String> taskArgs, String key, String defaultValue) {
        if (taskArgs.containsKey(key)) {
            return taskArgs.get(key);
        } else {
            return defaultValue;
        }
    }

    public static String defaultIfEmpty(Map<String, String> taskArgs, String key, String defaultValue) {
        if (taskArgs.containsKey(key)) {
            String value = taskArgs.get(key);
            if (!Strings.isNullOrEmpty(value)) {
                return value;
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
}