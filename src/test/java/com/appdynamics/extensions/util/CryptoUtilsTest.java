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

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.appdynamics.extensions.Constants.*;
import static com.appdynamics.extensions.SystemPropertyConstants.ENCRYPTION_KEY_PROPERTY;

/**
 * Created by venkata.konala on 1/11/19.
 */
public class CryptoUtilsTest {

    @Test
    public void whenPasswordPresentReturnPassword() {
        String password = CryptoUtils.getPassword(getConfigMap());
        Assert.assertEquals(password, "password");
    }

    @Test
    public void whenPasswordNotPresentReturnDecryptedPassword() {
        Map<String, ?> configMap = getConfigMap();
        configMap.remove(PASSWORD);
        String password = CryptoUtils.getPassword(configMap);
        Assert.assertEquals(password, "decryptedPassword");
    }

    @Test
    public void whenEmptyPasswordPresentReturnDecryptedPassword() {
        Map<String, Object> configMap = getConfigMap();
        configMap.put(PASSWORD, "");
        String password = CryptoUtils.getPassword(configMap);
        Assert.assertEquals(password, "decryptedPassword");
    }

    @Test
    public void whenEmptyEncryptionKeyShouldGetItFromSysPropsAndReturnDecryptedPassword() {
        Map<String, Object> configMap = getConfigMap();
        configMap.put(PASSWORD, "");
        configMap.put(ENCRYPTION_KEY, "");
        System.setProperty(ENCRYPTION_KEY_PROPERTY, "encryptionKey");
        String password = CryptoUtils.getPassword(configMap);
        Assert.assertEquals(password, "decryptedPassword");
        System.clearProperty(ENCRYPTION_KEY_PROPERTY);
    }

    @Test
    public void whenNullEncryptionKeyShouldGetItFromSysPropsAndReturnDecryptedPassword() {
        Map<String, Object> configMap = getConfigMap();
        configMap.put(PASSWORD, "");
        configMap.remove(ENCRYPTION_KEY);
        System.setProperty(ENCRYPTION_KEY_PROPERTY, "encryptionKey");
        String password = CryptoUtils.getPassword(configMap);
        Assert.assertEquals(password, "decryptedPassword");
        System.clearProperty(ENCRYPTION_KEY_PROPERTY);
    }

    @Test
    public void whenNullPasswordAndNullEncryptedPasswordShouldReturnEmptyPassword() {
        Map<String, Object> configMap = getConfigMap();
        configMap.put(PASSWORD, "");
        configMap.put(ENCRYPTED_PASSWORD, "");
        System.setProperty(ENCRYPTION_KEY_PROPERTY, "encryptionKey");
        String password = CryptoUtils.getPassword(configMap);
        Assert.assertEquals(password, "");
    }

    private Map<String, Object> getConfigMap() {
        Map<String, Object> configMap = Maps.newHashMap();
        configMap.put(PASSWORD, "password");
        configMap.put(ENCRYPTED_PASSWORD, "/EQauq9NLV0MFYd46JPcLgWxCjlfrRWBe1+83s0LiVU=");
        configMap.put(ENCRYPTION_KEY, "encryptionKey");
        return configMap;
    }

}