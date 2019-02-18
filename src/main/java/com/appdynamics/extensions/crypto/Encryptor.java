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

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;


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
        if ((args != null) && (args.length == 2)) {
            Encryptor encryptor = new Encryptor(args[0]);
            String encrypted = encryptor.encrypt(args[1]);
            System.out.println("***************Encrypted String***************");
            System.out.println(encrypted);
            System.out.println("**********************************************");
        }

        else {
            //System.out.println("usage:  java -cp appd-exts-commons-1.1.0.jar com.appdynamics.extensions.encrypt.Encryptor <myKey> <myPassword>");
            String args0 = System.getProperty("ENCRYPTION_KEY");
            Encryptor encryptor = new Encryptor(args0);
            String args1 = System.getProperty("PLAINTEXT_PWD");
            String encrypted = encryptor.encrypt(args1);
            System.out.println("***************Encrypted String***************");
            System.out.println(encrypted);
            System.out.println("**********************************************");
        }
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(Throwable cause) {
            super(cause);
        }
    }
}
