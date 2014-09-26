package com.appdynamics.extensions.crypto;

import org.junit.Assert;
import org.junit.Test;

public class EncryptDecryptTest {

    private static final String PLAIN_TEXT = "This is a plain text.";
    private static final String ENCRYPTION_KEY = "HelloWorld";

    @Test
    public void canEncryptDecryptText(){
        Encryptor encryptor = new Encryptor(ENCRYPTION_KEY);
        String encryptedText = encryptor.encrypt(PLAIN_TEXT);
        Decryptor decryptor = new Decryptor(ENCRYPTION_KEY);
        Assert.assertTrue(PLAIN_TEXT.equals(decryptor.decrypt(encryptedText)));
    }
}
