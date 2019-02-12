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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.Constants.ENCRYPTED_PASSWORD;
import static com.appdynamics.extensions.Constants.ENCRYPTION_KEY;
import static com.appdynamics.extensions.Constants.PASSWORD;
import static com.appdynamics.extensions.SystemPropertyConstants.*;

/**
 * Created by venkata.konala on 1/5/19.
 */
public class SSLUtils {

    private static Logger logger = ExtensionsLoggerFactory.getLogger(SSLUtils.class);

    public static SSLContext createSSLContext(File installDir, Map<String, ?> propMap) throws Exception {
        try {
            Map<String, ?> connectionMap = (Map<String, ?>)propMap.get("connection");
            String encryptionKey = (String)propMap.get("encryptionKey");
            //#TODO Add protocols from connections.
            SSLContext context = SSLContext.getInstance(getSSLProtocol(connectionMap));

            KeyStore keyStore = buildKeyStore(installDir, connectionMap, encryptionKey);
            if(keyStore == null) {
                logger.info("Couldn't resolve the keystore. It will be null");
            }
            char[] keyStorePassword = getKeyStorePassword(connectionMap, encryptionKey);

            KeyStore trustStore = buildTrustStore(installDir, connectionMap, encryptionKey);
            if(trustStore == null) {
                logger.info("Couldn't resolve the truststore. The default jre truststore will be used");
            }

            //#TODO Check for an empty keystore.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            context.init( keyManagers, trustManagers, new SecureRandom());
            return context;
        } catch (Exception e) {
            throw e;
        }
    }

    private static String getSSLProtocol(Map<String, ?> connectionMap) {
        if(connectionMap != null && (List)connectionMap.get("sslProtocols") != null) {
            List<String> sslProtocols = (List)connectionMap.get("sslProtocols");
            String protocol = sslProtocols.get(0);
            if(!Strings.isNullOrEmpty(protocol)) {
                return protocol;
            }
        }
        return "TLS";
    }

    //#TODO Need to fall back on ma-cacerts as well
    public static KeyStore buildKeyStore(File installDir, Map<String, ?> connectionMap, String encryptionKey) {
        KeyStore keyStore;
        File file = getKeyStoreFile(installDir, connectionMap);
        if(file != null && file.exists()) {
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                char[] keyStorePassword = getKeyStorePassword(connectionMap, encryptionKey);
                keyStore.load(new FileInputStream(file), keyStorePassword);
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.warn("Exception while creating a custom keystore. Will return null", e);
            }
        }
        logger.warn("The KeyStore has not been built properly, returning null");
        return null;
    }

    private static File getKeyStoreFile(File installDir, Map<String, ?> connectionMap) {
        File file = null;
        if (connectionMap != null && connectionMap.get("sslKeyStorePath") != null) {
            file = PathResolver.getFile((String)connectionMap.get("sslKeyStorePath"), installDir);
        }
        if (file == null || !file.exists()) {
            logger.debug("The sslKeyStorePath field is either null or the file specified does not exist. Checking {} property", KEYSTORE_PATH_PROPERTY);
            file = PathResolver.getFile(System.getProperty(KEYSTORE_PATH_PROPERTY), installDir);
        }
        if(file == null || !file.exists()) {
            logger.debug("The {} property is null or the file specified does not exist. Checking conf/extensions-clientcerts.jks", KEYSTORE_PATH_PROPERTY);
            file = PathResolver.getFile("conf/extensions-clientcerts.jks", installDir);
        }
        if(file == null || !file.exists()) {
            logger.debug("The conf/extensions-clientcerts.jks is null or does not exist.");
        }
        return file;
    }

    public static char[] getKeyStorePassword(Map<String, ?> connectionMap, String encryptionKey) {
        if (connectionMap != null) {
            String password = (String)connectionMap.get("sslKeyStorePassword");
            String encryptedPassword = (String)connectionMap.get("sslKeyStoreEncryptedPassword");
            if (Strings.isNullOrEmpty(password)) {
                password = System.getProperty(KEYSTORE_PASSWORD_PROPERTY);
            }
            if(Strings.isNullOrEmpty(encryptedPassword)) {
                encryptedPassword = System.getProperty(KEYSTORE_ENCRYPTED_PASSWORD_PROPERTY);
            }
            Map<String, Object> passwordMap = Maps.newHashMap();
            passwordMap.put(PASSWORD, password);
            passwordMap.put(ENCRYPTED_PASSWORD, encryptedPassword);
            passwordMap.put(ENCRYPTION_KEY, encryptionKey);
            String keystorePassword = CryptoUtils.getPassword(passwordMap);
            return Strings.isNullOrEmpty(keystorePassword) ? null : keystorePassword.toCharArray();
        }
        logger.warn("Returning null password for sslKeyStore");
        return null;
    }

    //#TODO Need to fall back on ma-cacerts as well
    // #TODO Add extensions-cacerts
    public static KeyStore buildTrustStore(File installDir, Map<String, ?> connectionMap, String encryptionKey) {
        KeyStore trustStore;
        File file = getTrustStoreFile(installDir, connectionMap);
        if(file != null && file.exists()) {
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                char[] trustStorePassword = getTrustStorePassword(connectionMap, encryptionKey);
                trustStore.load(new FileInputStream(file), trustStorePassword);
                return trustStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.warn("Exception while creating a custom truststore. Will fallback on the jre default truststore if available", e);
            }
        }
        logger.warn("The TrustStore has not been built properly, returning null");
        return null;
    }

    private static File getTrustStoreFile(File installDir, Map<String, ?> connectionMap) {
        File file = null;
        if (connectionMap != null && connectionMap.get("sslTrustStorePath") != null) {
            file = PathResolver.getFile((String)connectionMap.get("sslTrustStorePath"), installDir);
        }
        if (file == null || !file.exists()) {
            logger.debug("The sslTrustStorePath field is either null or the file specified does not exist. Checking {} property", TRUSTSTORE_PATH_PROPERTY);
            file = PathResolver.getFile(System.getProperty(TRUSTSTORE_PATH_PROPERTY), installDir);
        }
        if(file == null || !file.exists()) {
            logger.debug("The {} property is null or the file specified does not exist. Checking conf/extensions-cacerts.jks", TRUSTSTORE_PATH_PROPERTY);
            file = PathResolver.getFile("conf/extensions-cacerts.jks", installDir);
        }
        if(file == null || !file.exists()) {
            logger.debug("The conf/extensions-cacerts.jks is null or does not exist.");
        }
        return file;
    }

    public static char[] getTrustStorePassword(Map<String, ?> connectionMap, String encryptionKey) {
        if (connectionMap != null) {
            String password = (String)connectionMap.get("sslTrustStorePassword");
            String encryptedPassword = (String)connectionMap.get("sslTrustStoreEncryptedPassword");
            if (Strings.isNullOrEmpty(password)) {
                password = System.getProperty(TRUSTSTORE_PASSWORD_PROPERTY);
            }
            if(Strings.isNullOrEmpty(encryptedPassword)) {
                encryptedPassword = System.getProperty(TRUSTSTORE_ENCRYPTED_PASSWORD_PROPERTY);
            }
            Map<String, Object> passwordMap = Maps.newHashMap();
            passwordMap.put(PASSWORD, password);
            passwordMap.put(ENCRYPTED_PASSWORD, encryptedPassword);
            passwordMap.put(ENCRYPTION_KEY, encryptionKey);
            String truststorePassword = CryptoUtils.getPassword(passwordMap);
            return Strings.isNullOrEmpty(truststorePassword) ? null : truststorePassword.toCharArray();
        }
        logger.warn("Returning null password for sslTrustStore");
        return null;
    }

    public static HostnameVerifier createHostNameVerifier(Map<String, ?> connectionMap) {
        if(connectionMap != null && connectionMap.get("sslVerifyHostname") != null && (Boolean)connectionMap.get("sslVerifyHostname")) {
            return new BrowserCompatHostnameVerifier();
        }
        return new AllHostnameVerifier();
    }

    public static class AllHostnameVerifier implements X509HostnameVerifier {
        public void verify(String host, SSLSocket ssl)
                throws IOException {
        }

        public void verify(String host, X509Certificate cert)
                throws SSLException {
        }

        public void verify(String host, String[] cns,
                           String[] subjectAlts) throws SSLException {
        }

        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
