package com.appdynamics.extensions.crypto;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
        } catch (BadPaddingException e) {
            log.error(e);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        } catch (IllegalBlockSizeException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        return "";
    }


}
