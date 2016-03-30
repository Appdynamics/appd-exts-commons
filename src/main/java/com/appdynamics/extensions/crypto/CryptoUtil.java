package com.appdynamics.extensions.crypto;

import com.google.common.base.Strings;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.appdynamics.TaskInputArgs.*;

public class CryptoUtil {
    public static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
    private static URLCodec codec = new URLCodec("UTF-8");

    public static final String SYSTEM_ARG_KEY = "appdynamics.extensions.key";

    public static String getPassword(Map<String,String> taskArgs){
        if(taskArgs.containsKey(PASSWORD)){
            return taskArgs.get(PASSWORD);
        } else if(taskArgs.containsKey(PASSWORD_ENCRYPTED)){
            String encryptedPassword = taskArgs.get(PASSWORD_ENCRYPTED);
            String encryptionKey = taskArgs.get(ENCRYPTION_KEY);
            if(Strings.isNullOrEmpty(encryptionKey)){
                encryptionKey = System.getProperty(SYSTEM_ARG_KEY);
            }
            if(!Strings.isNullOrEmpty(encryptionKey)){
                return new Decryptor(encryptionKey).decrypt(encryptedPassword);
            } else{
                String msg = "Encryption Key not specified. Please set the property 'encryption-key' in monitor.xml or add the System Property '-Dappdynamics.extensions.key'";
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }
        }
        return "";
    }

    public static String encode(String val) {
        if (!Strings.isNullOrEmpty(val)) {
            try {
                return codec.encode(val);
            } catch (EncoderException e) {
                logger.error("Error while encoding the value [cant log might be a password]", e);
            }
        }
        return val;
    }

}
