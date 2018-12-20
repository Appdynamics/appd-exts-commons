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

package com.appdynamics.extensions.http;

import static com.appdynamics.extensions.Constants.AUTH_TYPE;
import static com.appdynamics.extensions.Constants.USER;

import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthenticationConfig {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(AuthenticationConfig.class);

    private AuthenticationMode mode;
    private String authHeader;

    public static enum AuthenticationMode {
        BASIC
    }

    public static AuthenticationConfig build(Map<String, String> taskArgs) {
        String user = taskArgs.get(USER);
        if (!Strings.isNullOrEmpty(user)) {
            String password = CryptoUtil.getPassword(taskArgs);
            if (Strings.isNullOrEmpty(password)) {
                password = "";
                logger.warn("The password is empty, empty string will be used as the password");
            }
            String authType = taskArgs.get(AUTH_TYPE);
            if (Strings.isNullOrEmpty(authType)) {
                authType = AuthenticationMode.BASIC.toString();
                logger.debug("The authentication type is not set, defaulting to BASIC");
            }
            AuthenticationConfig config = new AuthenticationConfig();
            AuthenticationMode authMode = config.getAuthenticationMode(authType);
            logger.debug("The authentication is set user= " + user + " Auth type " + authMode);
            config.buildAuthentication(user, password, authMode);
            return config;
        } else {
            logger.debug("The authentication is not enabled.");
        }
        return null;
    }


    private void buildAuthentication(String user, String password, AuthenticationMode authMode) {
        this.mode = authMode;
        StringBuilder sb = new StringBuilder();
        sb.append(user).append(":").append(password);
        this.authHeader = "Basic " + new String(Base64.encodeBase64(sb.toString().getBytes()));
    }

    protected AuthenticationMode getAuthenticationMode(String authType) {
        try {
            return AuthenticationMode.valueOf(authType);
        } catch (IllegalArgumentException e) {
            String msg = "Unknown Authentication Mode Set in the task arguments '%s'. The valid values are %s";
            throw new IllegalArgumentException(String.format(msg, authType, AuthenticationMode.BASIC));
        }
    }

    public AuthenticationMode getMode() {
        return mode;
    }

    public String getAuthHeader() {
        return authHeader;
    }
}
