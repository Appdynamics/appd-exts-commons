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

import com.google.common.base.Strings;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;

import static com.appdynamics.extensions.SystemPropertyConstants.ENCRYPTION_KEY_PROPERTY;
import static com.appdynamics.extensions.SystemPropertyConstants.PLAIN_TEXT_PROPERTY;



public class Encryptor {

    private Cipher cipher;

    public Encryptor(String encryptionKey) {
        try {
            cipher = CipherFactory.getInstance().createCipher(encryptionKey, Cipher.ENCRYPT_MODE);
        } catch (CipherInitException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String plainText) {
        try {
            byte[] utf8 = plainText.getBytes("UTF-8");
            byte[] enc = cipher.doFinal(utf8);
            return new BASE64Encoder().encode(enc);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }


    public static void main(String[] args) {
        String encryptionKey;
        String plainText;

        if ((args != null) && (args.length == 2)) {
            encryptionKey = args[0];
            plainText = args[1];
        } else {
            encryptionKey = System.getProperty(ENCRYPTION_KEY_PROPERTY);
            plainText = System.getProperty(PLAIN_TEXT_PROPERTY);
        }

        if(!Strings.isNullOrEmpty(encryptionKey) && !Strings.isNullOrEmpty(plainText)) {
            Encryptor encryptor = new Encryptor(encryptionKey);
            String encryptedText = encryptor.encrypt(plainText);
            System.out.println("****************************Encrypted Text**************************");
            System.out.println(encryptedText);
            System.out.println("********************************************************************");
            return;
        }
        System.out.println("usage: java -cp <monitoring-extension.jar> com.appdynamics.extensions.crypto.Encryptor <myEncryptionKey> <myClearTextPassword>    (or)");
        System.out.println("usage: java -Dappdynamics.agent.monitors.encryptionKey=<myEncryptionKey> -Dappdynamics.agent.monitors.plainText=<myClearTextPassword> -cp <monitoring-extension.jar> com.appdynamics.extensions.crypto.Encryptor");
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(Throwable cause) {
            super(cause);
        }
    }
}
