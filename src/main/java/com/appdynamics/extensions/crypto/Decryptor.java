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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import java.util.Arrays;
import java.util.Base64;

public class Decryptor {

    private final String encryptionKey;
    private static final Log log = LogFactory.getLog(Decryptor.class);

    public Decryptor(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedText);
            byte[] iv;
            byte[] cipherBytes;
            // New format: [version=0x01][iv 16 bytes][ciphertext]
            if (payload.length > 17 && payload[0] == CipherFactory.FORMAT_VERSION) {
                iv = Arrays.copyOfRange(payload, 1, 17);
                cipherBytes = Arrays.copyOfRange(payload, 17, payload.length);
            } else {
                // Legacy format: raw ciphertext encrypted with static IV
                iv = CipherFactory.LEGACY_IV;
                cipherBytes = payload;
            }
            Cipher cipher = CipherFactory.getInstance().createCipher(encryptionKey, iv, Cipher.DECRYPT_MODE);
            byte[] dec = cipher.doFinal(cipherBytes);
            return new String(dec, "UTF-8");
        } catch (Exception e) {
            throw new DecryptionException("Error while decrypting the value [" + encryptedText + "]", e);
        }
    }

    public static class DecryptionException extends RuntimeException{
        public DecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
