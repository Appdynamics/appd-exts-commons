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

package com.appdynamics.extensions.crypto;

import org.junit.Assert;
import org.junit.Test;

public class EncryptDecryptTest {

    @Test
    public void encryptProducesRandomIvFormatRoundTrip() {
        Encryptor encryptor = new Encryptor("encryptionKey");
        String encrypted = encryptor.encrypt("plainText");
        // New format includes version byte + random IV, so result is non-deterministic and longer than legacy
        Assert.assertFalse("avQa9cYNOoO6Ba1p3He+HQ==".equals(encrypted));
        Decryptor decryptor = new Decryptor("encryptionKey");
        Assert.assertEquals("plainText", decryptor.decrypt(encrypted));
    }

    @Test
    public void decryptLegacyCiphertextWithStaticIv() {
        // Values encrypted before this fix (static IV) must still decrypt correctly
        Decryptor decryptor = new Decryptor("encryptionKey");
        String plainTextRetrieved = decryptor.decrypt("avQa9cYNOoO6Ba1p3He+HQ==");
        Assert.assertEquals("plainText", plainTextRetrieved);
    }

    @Test
    public void encryptTwiceShouldProduceDifferentCiphertexts() {
        Encryptor encryptor = new Encryptor("encryptionKey");
        String first = encryptor.encrypt("plainText");
        String second = encryptor.encrypt("plainText");
        Assert.assertFalse("Same plaintext should produce different ciphertexts with random IV", first.equals(second));
    }
}
