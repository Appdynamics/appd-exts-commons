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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;

public class Decryptor {

    private Cipher cipher;
    private static final Log log = LogFactory.getLog(Decryptor.class);

    public Decryptor(String encryptionKey){
        try {
            cipher = CipherFactory.getInstance().createCipher(encryptionKey, Cipher.DECRYPT_MODE);
        } catch (CipherInitException e) {
            log.error("Unable to create Cipher " + e);
        }
    }


    public String decrypt(String encryptedText) {
        try {
            byte[] bytes = new BASE64Decoder().decodeBuffer(encryptedText);
            byte[] enc = cipher.doFinal(bytes);
            return new String(enc, "UTF-8");
        } catch (Exception e) {
            throw new DecryptionException("Error while decrypting the value ["+encryptedText+"]",e);
        }
    }

    public static class DecryptionException extends RuntimeException{
        public DecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
