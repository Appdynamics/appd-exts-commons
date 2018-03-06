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
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class TaskInputArgs {
    public static final String USER = "username";
    public static final String PASSWORD = "password";
    public static final String ENCRYPTED_PASSWORD = "encryptedPassword";
    public static final String AUTH_TYPE = "auth-type";
    public static final String HOST = "host";
    public static final String URI = "uri";
    public static final String PORT = "port";
    public static final String USE_SSL = "useSsl";
    public static final String DISABLE_SSL_CERT_VALIDATION = "disable-ssl-cert-validation";
    public static final String METRIC_PREFIX = "metric-prefix";
    public static final String TRUST_STORE_FILE = "trust-store-file";
    public static final String TRUST_STORE_PWD = "trust-store-password";
    public static final String SSL_VERIFY_HOSTNAME = "ssl-verify-hostname";
    public static final String PROXY_URI = "proxy-uri";
    public static final String PROXY_HOST = "proxy-host";
    public static final String PROXY_PORT = "proxy-port";
    public static final String PROXY_USER = "proxy-username";
    public static final String PROXY_PASSWORD = "proxy-password";
    public static final String PROXY_PASSWORD_ENCRYPTED = "proxy-password-encrypted";
    public static final String PROXY_USE_SSL = "proxy-use-ssl";
    public static final String PROXY_AUTH_TYPE = "proxy-auth-type";
    public static final String ENCRYPTION_KEY = "encryptionKey";
    public static final String SSL_PROTOCOL = "ssl-protocol";

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
