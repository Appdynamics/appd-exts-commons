package com.appdynamics.extensions.http;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.crypto.Decryptor;
import com.google.common.base.Strings;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 6/30/15.
 */
public class Http4ClientBuilder {
    public static final Logger logger = LoggerFactory.getLogger(Http4ClientBuilder.class);

    public static HttpClientBuilder getBuilder(Map<String, ?> propMap) {
        return configureBuilder(HttpClients.custom(), propMap);
    }

    public static HttpClientBuilder configureBuilder(HttpClientBuilder builder, Map<String, ?> propMap) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        builder.setConnectionManager(new PoolingHttpClientConnectionManager());
        configureConnectionProps(propMap, builder);
        configureSSL(propMap, builder);
        configureProxy(propMap, builder, credsProvider);
        configureAuthentication(propMap, builder, credsProvider);
        builder.setDefaultCredentialsProvider(credsProvider);
        return builder;
    }

    private static void configureConnectionProps(Map<String, ?> propMap, HttpClientBuilder builder) {
        Map connection = (Map) propMap.get("connection");
        if (connection != null) {

            Integer socketTimeout = (Integer) connection.get("socketTimeout");
            if (socketTimeout == null) {
                socketTimeout = 5000;
            }
            Integer connectTimeout = (Integer) connection.get("connectTimeout");
            if (connectTimeout == null) {
                connectTimeout = 5000;
            }
            logger.info("Setting the connect timeout to {} and socket timeout to {}", connectTimeout, socketTimeout);
            RequestConfig.Builder configBuilder = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout);
            builder.setDefaultRequestConfig(configBuilder.build());

        }
    }

    public static void configureAuthentication(Map<String, ?> config, HttpClientBuilder builder
            , CredentialsProvider credsProvider) {
        List<Map<String, String>> servers = (List<Map<String, String>>) config.get("servers");
        if (servers != null && !servers.isEmpty()) {
            for (Map<String, String> server : servers) {
                String username = server.get(TaskInputArgs.USER);
                if (!Strings.isNullOrEmpty(username)) {
                    AuthScope authScope = createAuthScope(server);
                    if (authScope != null) {
                        credsProvider.setCredentials(
                                authScope,
                                new UsernamePasswordCredentials(username, getPassword(server, config)));
                        logger.info("Created credentials for auth scope {}", authScope);
                    }
                }
            }
        }
    }

    private static AuthScope createAuthScope(Map<String, String> server) {
        String uri = server.get("uri");
        if (!Strings.isNullOrEmpty(uri)) {
            try {
                URL url = new URL(uri);
                logger.info("Creating the auth scope for URI [{}]", uri);
                return new AuthScope(url.getHost(), url.getPort());
            } catch (MalformedURLException e) {
                logger.error("The url appears to be malformed " + uri, e);
                return null;
            }
        } else {
            String host = server.get("host");
            String port = server.get("port");
            if (!Strings.isNullOrEmpty(host) && !Strings.isNullOrEmpty(port)) {
                logger.info("Creating the auth scope for host [{}] and port [{}]", host, port);
                return new AuthScope(host, Integer.parseInt(port));
            } else {
                return null;
            }
        }
    }


    protected static void configureProxy(Map<String, ?> propMap, HttpClientBuilder builder, CredentialsProvider credsProvider) {
        Map<String, String> proxyMap = (Map<String, String>) propMap.get("proxy");
        if (proxyMap != null) {
            String proxyUri = proxyMap.get("uri");
            if (!Strings.isNullOrEmpty(proxyUri)) {
                try {
                    URL url = new URL(proxyUri);
                    HttpHost proxy = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                    String proxyUserName = proxyMap.get("username");
                    if (!Strings.isNullOrEmpty(proxyUserName)) {
                        String proxyPassword = getPassword(proxyMap, propMap);
                        credsProvider.setCredentials(
                                new AuthScope(url.getHost(), url.getPort()),
                                new UsernamePasswordCredentials(proxyUserName, proxyPassword));
                        builder.setDefaultCredentialsProvider(credsProvider);
                        logger.info("Configured the http client with the proxy [{}] and user [{}] and password [***]"
                                , proxyUri, proxyUserName);
                    } else {
                        logger.info("Configured the http client with the proxy [{}] and and no proxy credentials"
                                , proxyUri);
                    }
                    builder.setProxy(proxy);
                } catch (MalformedURLException e) {
                    logger.error("The proxy uri appears to be invalid " + proxyUri, e);
                }
            } else {
                logger.info("Not configuring proxy, the property [uri] is not set");
            }
        } else {
            logger.info("Not configuring proxy, the property [proxy] is not found");
        }
    }

    protected static String getPassword(Map<String, ?> propMap, Map<String, ?> config) {
        String password = (String) propMap.get("password");
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        } else {
            String encrypted = (String) propMap.get("passwordEncrypted");
            if (!Strings.isNullOrEmpty(encrypted)) {
                String encryptionKey = getEncryptionKey(config);
                if (!Strings.isNullOrEmpty(encryptionKey)) {
                    return new Decryptor(encryptionKey).decrypt(encrypted);
                } else {
                    logger.error("Cannot decrypt the password. Encryption key not set");
                    throw new RuntimeException("Cannot decrypt [passwordEncrypted], since [encryptionKey] is not set");
                }
            } else {
                logger.warn("No password set, using empty string");
                return "";
            }
        }
    }

    protected static String getEncryptionKey(Map<String, ?> propMap) {
        String encryptionKey = System.getProperty(CryptoUtil.SYSTEM_ARG_KEY);
        if (Strings.isNullOrEmpty(encryptionKey)) {
            encryptionKey = (String) propMap.get("encryptionKey");
            if (!Strings.isNullOrEmpty(encryptionKey)) {
                logger.debug("Read the [encryptionKey] from config.yml");
            }
        } else {
            logger.debug("Read the encryption key from the System Property");
        }
        return encryptionKey;
    }

    protected static void configureSSL(Map<String, ?> propMap, HttpClientBuilder builder) {
        Map connection = (Map) propMap.get("connection");
        if (connection != null) {
            String[] sslProtocols = asStringArray((String) connection.get("sslProtocols"));
            logger.info("The supported ssl protocols are {}", sslProtocols != null ? Arrays.toString(sslProtocols) : "default");
            String[] sslCipherSuites = asStringArray((String) connection.get("sslCipherSuites"));
            logger.info("The supported ssl cipher suites are {}", sslCipherSuites != null ? Arrays.toString(sslCipherSuites) : "default");
            Boolean sslCertCheckEnabled = (Boolean) connection.get("sslCertCheckEnabled");
            if (Boolean.FALSE.equals(sslCertCheckEnabled)) {
                logger.warn("Disabling the ssl certificate checks");
                try {
                    SSLContext sslContext = SSLContexts.custom()
                            .loadTrustMaterial(null, new TrustStrategy() {
                                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                    return true;
                                }
                            }).build();
                    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                            sslContext, sslProtocols, sslCipherSuites, new X509HostnameVerifier() {
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
                    });
                    Registry<ConnectionSocketFactory> socketFactoryRegistry =
                            RegistryBuilder.<ConnectionSocketFactory>create()
                                    .register("https", socketFactory)
                                    .register("http", new PlainConnectionSocketFactory()).build();
                    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                            socketFactoryRegistry);
                    builder.setSSLSocketFactory(socketFactory).setConnectionManager(cm);
                } catch (Exception e) {
                    logger.error("Error while configuring the SSL", e);
                }
            } else if (sslCipherSuites != null || sslProtocols != null) {
                logger.info("Adding support for SSL Cipher suites, with default SSL Configuration");
                try {
                    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                            SSLContext.getDefault(),
                            sslProtocols,
                            sslCipherSuites,
                            new BrowserCompatHostnameVerifier()
                    );
                    Registry<ConnectionSocketFactory> socketFactoryRegistry =
                            RegistryBuilder.<ConnectionSocketFactory>create()
                                    .register("https", socketFactory)
                                    .register("http", new PlainConnectionSocketFactory()).build();
                    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                            socketFactoryRegistry);
                    builder.setSSLSocketFactory(socketFactory).setConnectionManager(cm);
                } catch (Exception e) {
                    logger.error("Error while configuring the SSL", e);
                }
            }
        }
    }

    private static String[] asStringArray(String str) {
        if (!Strings.isNullOrEmpty(str)) {
            return str.trim().split(",");
        } else {
            return null;
        }
    }
}
