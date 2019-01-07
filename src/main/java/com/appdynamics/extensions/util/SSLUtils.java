package com.appdynamics.extensions.util;

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.crypto.Decryptor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by venkata.konala on 1/5/19.
 */
public class SSLUtils {

    private static Logger logger = ExtensionsLoggerFactory.getLogger(SSLUtils.class);

    public static SSLSocketFactory createSSLSocketFactory(Map<String, ?> propMap) throws Exception{
        Map<String, ?> connectionMap = (Map<String, ?>)propMap.get("connection");
        //#TODO Add protocols from connections.
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        KeyStore keyStore = buildKeyStore(connectionMap);
        char[] keyStorePassword = getKeyStorePassword(connectionMap, (String)propMap.get("encryptionKey"));
        KeyStore trustStore = buildTrustStore(connectionMap);

        KeyManager[] keyManagers = null;

        //#TODO Check for an empty keystore.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword);
        keyManagers = keyManagerFactory.getKeyManagers();

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        context.init( keyManagers, trustManagers, new SecureRandom());
        return context.getSocketFactory();
    }

    public static KeyStore buildKeyStore(Map<String, ?> connectionMap) {
        KeyStore keyStore;
        if(connectionMap != null && connectionMap.get("sslKeyStorePath") != null && connectionMap.get("sslKeyStorePassword") != null) {
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                String keyStorePath = (String)connectionMap.get("sslKeyStorePath");
                String keyStorePassword = (String)connectionMap.get("sslKeyStorePassword");
                InputStream inputStream = new FileInputStream(keyStorePath);
                keyStore.load(inputStream, keyStorePassword.toCharArray());
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Exception while creating a custom keystore. Will fallback on the jre default keystore if available", e);
            }
        }
        return null;
    }

    public static char[] getKeyStorePassword(Map<String, ?> connectionMap, String encryptionKey) {
        if (connectionMap != null) {
            String password = (String)connectionMap.get("sslKeyStorePassword");
            String encryptedPassword = (String)connectionMap.get("sslKeyStoreEncryptedPassword");
            if (!Strings.isNullOrEmpty(password)) {
                return password.toCharArray();
            } else if (!Strings.isNullOrEmpty(encryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(encryptedPassword).toCharArray();
            }
        }
        logger.warn("Returning null password for sslKeyStore");
        return null;
    }

    //#TODO Need to fall back on ma-cacerts as well
    // #TODO Add extensions-cacerts
    public static KeyStore buildTrustStore(Map<String, ?> connectionMap) {
        KeyStore trustStore;
        if(connectionMap != null && connectionMap.get("sslTrustStorePath") != null && connectionMap.get("sslTrustStorePassword") != null) {
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                String trustStorePath = (String)connectionMap.get("sslTrustStorePath");
                String trustStorePassword = (String)connectionMap.get("sslTrustStorePassword");
                InputStream inputStream = new FileInputStream(trustStorePath);
                trustStore.load(inputStream, trustStorePassword.toCharArray());
                return trustStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Exception while creating a custom truststore. Will fallback on the jre default truststore if available", e);
            }
        }
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
