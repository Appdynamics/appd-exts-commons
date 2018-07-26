/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

package com.appdynamics.extensions.http;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.crypto.Decryptor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.util.YmlUtils;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 6/30/15.
 * Builds the http client builder from the config.yml. A sample one is given at
 * appd-exts-commons/src/test/resources/expected.config.yml
 */
public class Http4ClientBuilder {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(Http4ClientBuilder.class);


    public static HttpClientBuilder getBuilder(String configYmlPath) {
        File file = PathResolver.getFile(configYmlPath, AManagedMonitor.class);
        if (file != null) {
            return getBuilder(file);
        } else {
            throw new RuntimeException("Cannot resolve the config path from" + configYmlPath);
        }
    }

    public static HttpClientBuilder getBuilder(File file) {
        Yaml yaml = new Yaml();
        try {
            Map<String, ?> propMap = (Map) yaml.load(new FileReader(file));
            return getBuilder(propMap);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while reading the contextConfiguration from " + getPath(file), e);
        }
    }

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

    private static void configureCookieStore(Map<String, ?> connection, HttpClientBuilder builder) {
        Boolean enableCookiesFlag = YmlUtils.getBoolean(connection.get("enableCookies"));
        // Default is false; unless explicitly enabled
        if (!Boolean.TRUE.equals(enableCookiesFlag)) {
            logger.debug("Setting the cookie store to no operation cookie store");
            builder.setDefaultCookieStore(new NoOpCookieStore());
        }
    }

    private static void configureConnectionProps(Map<String, ?> propMap, HttpClientBuilder builder) {
        Map connection = (Map) propMap.get("connection");
        //Make sure that the configs are initialized with default values
        if (connection == null) {
            connection = Collections.emptyMap();
        }
        configureTimeouts(builder, connection);
        configureCookieStore(propMap, builder);
        configurePreemptiveAuthentication(connection, builder);
    }

    private static void configurePreemptiveAuthentication(Map connection, HttpClientBuilder builder) {
        Boolean enablePreemptiveAuth = YmlUtils.getBoolean(connection.get("enablePreemptiveAuth"));
        //Default is true
        if (!Boolean.FALSE.equals(enablePreemptiveAuth)) {
            builder.addInterceptorLast(new PreemptiveAuthenticationInterceptor());
        }
    }

    private static void configureTimeouts(HttpClientBuilder builder, Map connection) {
        Integer socketTimeout = YmlUtils.getInteger(connection.get("socketTimeout"));
        if (socketTimeout == null) {
            socketTimeout = 5000;
        }
        Integer connectTimeout = YmlUtils.getInteger(connection.get("connectTimeout"));
        if (connectTimeout == null) {
            connectTimeout = 5000;
        }
        logger.info("Setting the connect timeout to {} and socket timeout to {}", connectTimeout, socketTimeout);
        RequestConfig.Builder configBuilder = RequestConfig.custom()
                .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout);
        builder.setDefaultRequestConfig(configBuilder.build());
    }

    public static void configureAuthentication(Map<String, ?> config, HttpClientBuilder builder
            , CredentialsProvider credsProvider) {
        List<Map<String, ?>> servers = (List<Map<String, ?>>) config.get("servers");
        if (servers != null && !servers.isEmpty()) {
            for (Map<String, ?> server : servers) {
                String authType = (String) server.get("authType");
                if (!StringUtils.hasText(authType) || "BASIC".equals(authType)) {
                    String username = (String) server.get(TaskInputArgs.USER);
                    AuthScope authScope = createAuthScope(server);
                    if (!Strings.isNullOrEmpty(username)) {
                        if (authScope != null) {
                            credsProvider.setCredentials(
                                    authScope,
                                    new UsernamePasswordCredentials(username, getPassword(server, config)));
                            logger.info("Created credentials for auth scope {}", authScope);
                        }
                    } else {
                        logger.info("Credentials are not set for {}", authScope);
                    }
                } else {
                    logger.info("Not setting up authentication for server {} since the authType is not set to BASIC", server.get("uri"));
                }
            }
        }
    }

    private static AuthScope createAuthScope(Map<String, ?> server) {
        String uri = (String) server.get("uri");
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
            String host = (String) server.get("host");
            Integer port = YmlUtils.getInteger(server.get("port"));
            if (!Strings.isNullOrEmpty(host) && port != null) {
                logger.info("Creating the auth scope for host [{}] and port [{}]", host, port);
                return new AuthScope(host, port);
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
                        logger.info("Configured the http client with the proxy [{}] and user [{}] and accountAccessKey [***]"
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

    public static String getPassword(Map<String, ?> propMap, Map<String, ?> config) {
        String password = (String) propMap.get("accountAccessKey");
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        } else {
            String encrypted = (String) propMap.get("encryptedPassword");
            if (!Strings.isNullOrEmpty(encrypted)) {
                String encryptionKey = getEncryptionKey(config);
                if (!Strings.isNullOrEmpty(encryptionKey)) {
                    return new Decryptor(encryptionKey).decrypt(encrypted);
                } else {
                    logger.error("Cannot decrypt the accountAccessKey. Encryption key not set");
                    throw new RuntimeException("Cannot decrypt [encryptedPassword], since [encryptionKey] is not set");
                }
            } else {
                logger.warn("No accountAccessKey set, using empty string");
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
            String[] sslProtocols = YmlUtils.asStringArray(connection.get("sslProtocols"));
            logger.info("The supported ssl protocols are {}", sslProtocols != null ? Arrays.toString(sslProtocols) : "default");
            String[] sslCipherSuites = YmlUtils.asStringArray(connection.get("sslCipherSuites"));
            logger.info("The supported ssl cipher suites are {}", sslCipherSuites != null ? Arrays.toString(sslCipherSuites) : "default");
            Boolean sslCertCheckEnabled = YmlUtils.getBoolean(connection.get("sslCertCheckEnabled"));
            Boolean verifyHostName = (Boolean) connection.get("sslVerifyHostname");
            if (Boolean.FALSE.equals(sslCertCheckEnabled)) {
                logger.warn("Disabling the ssl certificate checks");
                try {
                    SSLContext sslContext = SSLContexts.custom()
                            .loadTrustMaterial(null, new TrustAllStrategy()).build();
                    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                            sslContext, sslProtocols, sslCipherSuites, new AllHostnameVerifier());
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
            } else {
                X509HostnameVerifier hostnameVerifier;
                if (Boolean.FALSE.equals(verifyHostName)) {
                    hostnameVerifier = new AllHostnameVerifier();
                } else {
                    hostnameVerifier = new BrowserCompatHostnameVerifier();
                }
                KeyStore trustStore = loadDefaultTrustStore(propMap);
                //for client certificates in keystore aka mutual auth on ssl
                char[] keyStorePassword = getKeyStorePassword(propMap, connection);
                KeyStore keyStore = loadKeyStore(propMap, keyStorePassword);

                try {
                    SSLContext sslContext = getSslContext(trustStore, keyStore, keyStorePassword);
                    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                            sslContext,
                            sslProtocols,
                            sslCipherSuites,
                            hostnameVerifier
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
        } else {
            logger.debug("The connection properties are not set in the config.yml");
        }
    }


    private static SSLContext getSslContext(KeyStore trustStore, KeyStore keyStore, char[] keyStorePassword) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        if (trustStore != null) {
            sslContextBuilder.loadTrustMaterial(trustStore);
        }
        if (keyStore != null) {
            sslContextBuilder.loadKeyMaterial(keyStore, keyStorePassword);
        }
        return sslContextBuilder.build();
    }

    protected static KeyStore loadKeyStore(Map<String, ?> propMap, char[] keystorePassword) {
        Map<String, ?> connection = (Map<String, ?>) propMap.get("connection");
        File file = resolveKeyStorePath(connection);
        if (file != null && file.exists()) {
            try {
                logger.debug("Loading the keystore from [{}]", file.getAbsolutePath());
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(new FileInputStream(file), keystorePassword);
                return keystore;
            } catch (Exception e) {
                logger.error("Error while loading the keystore from " + file.getAbsolutePath(), e);
            }
        } else {
            logger.info("Couldn't resolve the keystore for extensions. The default jre keystore will be used");
        }
        return null;
    }


    protected static KeyStore loadDefaultTrustStore(Map<String, ?> propMap) {
        Map<String, ?> connection = (Map<String, ?>) propMap.get("connection");
        File file = resolveTrustStorePath(connection);
        if (file != null && file.exists()) {
            try {
                logger.debug("Loading the truststore from [{}]", file.getAbsolutePath());
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                char[] password = getTrustStorePassword(propMap, connection);
                keystore.load(new FileInputStream(file), password);
                return keystore;
            } catch (Exception e) {
                logger.error("Error while loading the truststore from " + file.getAbsolutePath(), e);
            }
        } else {
            logger.info("Couldn't resolve the truststore for extensions. The default jre truststore will be used");
        }
        return null;
    }

    protected static File resolveKeyStorePath(Map<String, ?> connection) {
        String property = System.getProperty("appdynamics.extensions.keystore.path");
        File installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
        File file = PathResolver.getFile(property, installDir);
        logger.debug("The system property [appdynamics.extensions.keystore.path] with value [{}] is resolved to file [{}]"
                , property, getPath(file));
        if (file == null || !file.exists()) {
            String sslKeyStore = (String) connection.get("sslKeyStorePath");
            file = PathResolver.getFile(sslKeyStore, installDir);
            logger.debug("The config property [sslKeyStorePath] with value [{}] is resolved to file [{}]"
                    , sslKeyStore, getPath(file));
        }
        if (file == null || !file.exists()) {
            file = PathResolver.getFile("conf/extensions-clientcerts.jks", installDir);
            if (file == null || !file.exists()) {
                logger.debug("The sslKeyStorePath [{}] doesn't exist", getPath(file));
            }
        }
        return file;
    }


    protected static File resolveTrustStorePath(Map<String, ?> connection) {
        String property = System.getProperty("appdynamics.extensions.truststore.path");
        File installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
        File file = PathResolver.getFile(property, installDir);
        logger.debug("The system property [appdynamics.extensions.truststore.path] with value [{}] is resolved to file [{}]"
                , property, getPath(file));
        if (file == null || !file.exists()) {
            String sslTrustStore = (String) connection.get("sslTrustStorePath");
            file = PathResolver.getFile(sslTrustStore, installDir);
            logger.debug("The config property [sslTrustStorePath] with value [{}] is resolved to file [{}]"
                    , sslTrustStore, getPath(file));
        }
        if (file == null || !file.exists()) {
            file = PathResolver.getFile("conf/extensions-cacerts.jks", installDir);
            if (file == null || !file.exists()) {
                logger.debug("The sslTrustStorePath [{}] doesn't exist", getPath(file));
            }
        }
        return file;
    }

    protected static char[] getKeyStorePassword(Map<String, ?> propMap, Map<String, ?> connection) {
        String sslKeyStorePassword = (String) connection.get("sslKeyStorePassword");
        if (!Strings.isNullOrEmpty(sslKeyStorePassword)) {
            return sslKeyStorePassword.toCharArray();
        } else {
            String sslKeyStorePasswordEncrypted = (String) connection.get("sslKeyStoreEncryptedPassword");
            String encryptionKey = getEncryptionKey(propMap);
            if (!Strings.isNullOrEmpty(sslKeyStorePasswordEncrypted) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(sslKeyStorePasswordEncrypted).toCharArray();
            } else {
                logger.warn("Returning null accountAccessKey for sslKeyStore. Please set the [connection.sslKeyStorePassword] or " +
                        "[connection.sslKeyStoreEncryptedPassword + encryptionKey]");
                return null;
            }
        }
    }


    protected static char[] getTrustStorePassword(Map<String, ?> propMap, Map<String, ?> connection) {
        String sslTrustStorePassword = (String) connection.get("sslTrustStorePassword");
        if (!Strings.isNullOrEmpty(sslTrustStorePassword)) {
            return sslTrustStorePassword.toCharArray();
        } else {
            String sslTrustStoreEncryptedPassword = (String) connection.get("sslTrustStoreEncryptedPassword");
            String encryptionKey = getEncryptionKey(propMap);
            if (!Strings.isNullOrEmpty(sslTrustStoreEncryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(sslTrustStoreEncryptedPassword).toCharArray();
            } else {
                logger.warn("Returning null accountAccessKey for sslTrustStore. Please set the [connection.sslTrustStorePassword] or " +
                        "[connection.sslTrustStoreEncryptedPassword + encryptionKey]");
                return null;
            }
        }
    }

    private static Object getPath(File file) {
        return file != null ? file.getAbsolutePath() : null;
    }

    private static class TrustAllStrategy implements TrustStrategy {
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    }

    private static class AllHostnameVerifier implements X509HostnameVerifier {
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

    private static class NoOpCookieStore implements CookieStore {
        private static final List<Cookie> cookies = Collections.emptyList();

        public void addCookie(Cookie cookie) {
        }

        public List<Cookie> getCookies() {
            return cookies;
        }

        public boolean clearExpired(Date date) {
            return true;
        }

        public void clear() {
        }
    }

    private static class PreemptiveAuthenticationInterceptor implements HttpRequestInterceptor {
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
            if (authState != null && authState.getAuthScheme() == null) {
                BasicScheme authScheme = new BasicScheme();
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                if (credsProvider != null && targetHost != null) {
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                    if (creds != null) {
                        authState.update(authScheme, creds);
                        logger.debug("Added the BasicScheme to uri [{}]", targetHost);
                    } else {
                        logger.debug("Cannot add PreemptiveAuth; No credentials exist for host {} and port {}"
                                , targetHost.getHostName(), targetHost.getPort());
                    }
                } else {
                    if (targetHost == null) {
                        logger.debug("Cannot add PreemptiveAuth; The context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST) is null");
                    }
                    if (credsProvider == null) {
                        logger.debug("Cannot add PreemptiveAuth; The context.getAttribute(HttpClientContext.CREDS_PROVIDER)");
                    }
                }
            } else {
                if (authState != null && authState.getAuthScheme() != null) {
                    logger.debug("Cannot add PreemptiveAuth; AuthScheme already exists for url {}", request.getRequestLine());
                } else {
                    logger.debug("Cannot add PreemptiveAuth; The context.getAttribute(HttpClientContext.TARGET_AUTH_STATE) is null");
                }
            }
        }
    }
}
