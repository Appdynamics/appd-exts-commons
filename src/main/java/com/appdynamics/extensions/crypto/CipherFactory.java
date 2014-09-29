package com.appdynamics.extensions.crypto;


import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


class CipherFactory {

    private static final CipherFactory _instance = new CipherFactory();

    private static final byte[] INITIALIZATION_VECTOR = {(byte) 0xC2, (byte) 0xB9, (byte) 0xC8, (byte) 0x52,
            (byte) 0x96, (byte) 0x14, (byte) 0xE4, (byte) 0x53, (byte) 0x54, (byte) 0xC9, (byte) 0x76, (byte) 0x67,
            (byte) 0x78, (byte) 0x94, (byte) 0x12, (byte) 0x32};

    static CipherFactory getInstance(){
        return _instance;
    }

    Cipher createCipher(String encryptionKey, int mode) throws CipherInitException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] hash = getHash(encryptionKey);
            SecretKey secret = new SecretKeySpec(hash, "AES");
            cipher.init(mode, secret,new IvParameterSpec(INITIALIZATION_VECTOR));
            return cipher;
        }
        catch (NoSuchAlgorithmException e) {
            throw new CipherInitException(e);
        } catch (InvalidKeyException e) {
            throw new CipherInitException(e);
        }  catch (NoSuchPaddingException e) {
            throw new CipherInitException(e);
        }  catch (InvalidAlgorithmParameterException e) {
            throw new CipherInitException(e);
        }
    }

    private byte[] getHash(String encryptionKey) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passBytes = encryptionKey.getBytes();
        byte[] passHash = sha256.digest(passBytes);
        /* SHA-256 gives a 256-bit hash or 32 byte hash. AES by default supports 128-bit.
           To support larger keysize JCE unlimited policy files need to be placed in JRE-HOME/lib/security dir.
           http://www.informit.com/articles/article.aspx?p=170967&seqNum=11.
           To avoid that, extracting the last 16 bytes (128-bit) from a 32 byte word hash.
         */
        return get16ByteHash(passHash);
    }

    private byte[] get16ByteHash(byte[] passHash) {
        byte[] hash_16_byte = new byte[16];
        System.arraycopy(passHash,16,hash_16_byte,0,16);
        return hash_16_byte;
    }


}
