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
        } else {
            System.out.println("usage:  java -cp appd-exts-commons-1.1.0.jar com.appdynamics.extensions.encrypt.Encryptor <myKey> <myPassword>");
        }
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(Throwable cause) {
            super(cause);
        }
    }
}
