package com.appdynamics.extensions.util;

import com.appdynamics.extensions.crypto.Decryptor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
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

import static com.appdynamics.extensions.SystemPropertyConstants.KEYSTORE_PATH;

/**
 * Created by venkata.konala on 1/5/19.
 */
public class SSLUtils {

    private static Logger logger = ExtensionsLoggerFactory.getLogger(SSLUtils.class);

    public static SSLSocketFactory createSSLSocketFactory(File installDir, Map<String, ?> propMap) throws Exception {
        try {
            Map<String, ?> connectionMap = (Map<String, ?>)propMap.get("connection");
            String encryptionKey = (String)propMap.get("encryptionKey");
            //#TODO Add protocols from connections.
            SSLContext context = SSLContext.getInstance(getSSLProtocol(connectionMap));
            KeyStore keyStore = buildKeyStore(installDir, connectionMap, encryptionKey);
            String keyStorePassword = getKeyStorePassword(connectionMap, encryptionKey);
            KeyStore trustStore = buildTrustStore(installDir, connectionMap, encryptionKey);

            //#TODO Check for an empty keystore.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword == null ? null : keyStorePassword.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            context.init( keyManagers, trustManagers, new SecureRandom());
            return context.getSocketFactory();
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
        return "default";
    }

    //#TODO Need to fall back on ma-cacerts as well
    public static KeyStore buildKeyStore(File installDir, Map<String, ?> connectionMap, String encryptionKey) {
        KeyStore keyStore;
        File file = getKeyStoreFile(installDir, connectionMap);
        if(file != null && file.exists()) {
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                String keyStorePath = (String)connectionMap.get("sslKeyStorePath");
                String keyStorePassword = getKeyStorePassword(connectionMap, encryptionKey);
                InputStream inputStream = new FileInputStream(keyStorePath);
                keyStore.load(inputStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Exception while creating a custom keystore. Will fallback on the jre default keystore if available", e);
            }
        }
        return null;
    }

    public static File getKeyStoreFile(File installDir, Map<String, ?> connectionMap) {
        File file = null;
        if (connectionMap != null && connectionMap.get("sslKeyStorePath") != null && connectionMap.get("sslKeyStorePassword") != null) {
            file = PathResolver.getFile((String)connectionMap.get("sslKeyStorePath"), installDir);
        }
        if (file == null || !file.exists()) {
            file = PathResolver.getFile(System.getProperty(KEYSTORE_PATH), installDir);
        }
        if(file == null || !file.exists()) {
            file = PathResolver.getFile("conf/extensions-clientcerts.jks", installDir);
        }
        return file;
    }

    public static String getKeyStorePassword(Map<String, ?> connectionMap, String encryptionKey) {
        if (connectionMap != null) {
            String password = (String)connectionMap.get("sslKeyStorePassword");
            String encryptedPassword = (String)connectionMap.get("sslKeyStoreEncryptedPassword");
            if (!Strings.isNullOrEmpty(password)) {
                return password;
            } else if (!Strings.isNullOrEmpty(encryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(encryptedPassword);
            }
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
                String trustStorePath = (String)connectionMap.get("sslTrustStorePath");
                String trustStorePassword = getTrustStorePassword(connectionMap, encryptionKey);
                InputStream inputStream = new FileInputStream(trustStorePath);
                trustStore.load(inputStream, trustStorePassword == null ? null : trustStorePassword.toCharArray());
                return trustStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Exception while creating a custom truststore. Will fallback on the jre default truststore if available", e);
            }
        }
        return null;
    }

    public static File getTrustStoreFile(File installDir, Map<String, ?> connectionMap) {
        File file = null;
        if (connectionMap != null && connectionMap.get("sslTrustStorePath") != null && connectionMap.get("sslTrustStorePassword") != null) {
            file = PathResolver.getFile((String)connectionMap.get("sslTrustStorePath"), installDir);
        }
        if (file == null || !file.exists()) {
            file = PathResolver.getFile(System.getProperty(KEYSTORE_PATH), installDir);
        }
        if(file == null || !file.exists()) {
            file = PathResolver.getFile("conf/extensions-cacerts.jks", installDir);
        }
        return file;
    }

    public static String getTrustStorePassword(Map<String, ?> connectionMap, String encryptionKey) {
        if (connectionMap != null) {
            String password = (String)connectionMap.get("sslTrustStorePassword");
            String encryptedPassword = (String)connectionMap.get("sslTrustStoreEncryptedPassword");
            if (!Strings.isNullOrEmpty(password)) {
                return password;
            } else if (!Strings.isNullOrEmpty(encryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(encryptedPassword);
            }
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
