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

package com.appdynamics.extensions.crypto;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;

import java.util.Map;

import static com.appdynamics.extensions.Constants.*;

public class CryptoUtil {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CryptoUtil.class);
    private static URLCodec codec = new URLCodec("UTF-8");
    public static final String SYSTEM_ARG_KEY = "appdynamics.extensions.key";

    public static String getPassword(Map<String, String> taskArgs) {
        if (taskArgs.containsKey(PASSWORD)) {
            return taskArgs.get(PASSWORD);
        } else if (taskArgs.containsKey(ENCRYPTED_PASSWORD)) {
            String encryptedPassword = taskArgs.get(ENCRYPTED_PASSWORD);
            String encryptionKey = taskArgs.get(ENCRYPTION_KEY);
            if (Strings.isNullOrEmpty(encryptionKey)) {
                encryptionKey = System.getProperty(SYSTEM_ARG_KEY);
            }
            if (!Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(encryptedPassword);
            } else {
                String msg = "Encryption Key not specified. Please set the property 'encryption-key' in monitor.xml or add the System Property '-Dappdynamics.extensions.key'";
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }
        }
        return "";
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
