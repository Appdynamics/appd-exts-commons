package com.appdynamics.extensions.crypto;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


class CipherFactory {

    private static final CipherFactory _instance = new CipherFactory();

    private static final byte[] SALT = {(byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
            (byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03};
    private static final int ITERATION_COUNT = 1000;
    public static final String DEFAULT_ALGO = "PBEWithMD5AndDES";

    static CipherFactory getInstance(){
        return _instance;
    }

    Cipher createCipher(String encryptionKey, int mode) throws CipherInitException {
        try {
            KeySpec keySpec = new PBEKeySpec(encryptionKey.toCharArray(), SALT,
                    ITERATION_COUNT);
            SecretKey secretKey = SecretKeyFactory.getInstance(DEFAULT_ALGO).generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);
            cipher.init(mode, secretKey, paramSpec);
            return cipher;
        }
        catch (NoSuchAlgorithmException e) {
            throw new CipherInitException(e);
        } catch (InvalidKeyException e) {
            throw new CipherInitException(e);
        } catch (InvalidKeySpecException e) {
            throw new CipherInitException(e);
        } catch (NoSuchPaddingException e) {
            throw new CipherInitException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new CipherInitException(e);
        }
    }


}
