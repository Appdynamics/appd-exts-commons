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

import com.appdynamics.extensions.crypto.Decryptor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;

import java.util.Map;

import static com.appdynamics.extensions.Constants.*;
import static com.appdynamics.extensions.SystemPropertyConstants.ENCRYPTION_KEY_PROPERTY;

public class CryptoUtils {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CryptoUtils.class);
    private static URLCodec codec = new URLCodec("UTF-8");

    //#TODO Refactor the following
    /**
     * The getPassword(Map<String, ?> configMap) checks the configMap and returns the password
     * if present. If password is not present, then it falls back on the encryptedPassword and
     * encryptionKey(can be passed from {@link com.appdynamics.extensions.SystemPropertyConstants#ENCRYPTION_KEY_PROPERTY})
     * to generate the password.
     *
     * @return the password if present or an empty password.
     */
    public static String getPassword(Map<String, ?> configMap) {
        String password = (String)configMap.get(PASSWORD);
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        } else {
            String encryptedPassword = (String)configMap.get(ENCRYPTED_PASSWORD);
            String encryptionKey = (String)configMap.get(ENCRYPTION_KEY);
            if (Strings.isNullOrEmpty(encryptionKey)) {
                encryptionKey = System.getProperty(ENCRYPTION_KEY_PROPERTY);
            }
            if (!Strings.isNullOrEmpty(encryptionKey) && !Strings.isNullOrEmpty(encryptedPassword)) {
                return new Decryptor(encryptionKey).decrypt(encryptedPassword);
            }
        }
        logger.warn("The password has not been set properly. Using empty password.");
        return "";
    }

    //#TODO Refactor the following
    // #TODO Check pass by value and pass by reference.
    public static String getPassword(Map<String, ?> configMap, String encryptionKey) {
        Map<String, Object> configurationMap = Maps.newHashMap(configMap);
        configurationMap.put(ENCRYPTION_KEY, encryptionKey);
        return getPassword(configurationMap);
    }

    public static String encode(String val) {
        if (!Strings.isNullOrEmpty(val)) {
            try {
                return codec.encode(val);
            } catch (EncoderException e) {
                logger.error("Error while encoding the value [cant log might be a password]", e);
            }
        }
        return val;
    }
}
