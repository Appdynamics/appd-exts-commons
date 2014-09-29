package com.appdynamics.extensions.crypto;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.appdynamics.TaskInputArgs.PASSWORD;
import static com.appdynamics.TaskInputArgs.PASSWORD_ENCRYPTED;
import static com.appdynamics.TaskInputArgs.ENCRYPTION_KEY;

public class CryptoUtil {

    public static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
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

}
