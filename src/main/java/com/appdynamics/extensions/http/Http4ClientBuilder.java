package com.appdynamics.extensions.http;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.crypto.Decryptor;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
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
import org.yaml.snakeyaml.Yaml;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 6/30/15.
 * Builds the http client builder from the config.yml. A sample one is given at
 * appd-exts-commons/src/test/resources/expected.config.yml
 */
public class Http4ClientBuilder {
    public static final Logger logger = LoggerFactory.getLogger(Http4ClientBuilder.class);


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
            throw new RuntimeException("Error while reading the configuration from " + getPath(file), e);
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

    private static void configureConnectionProps(Map<String, ?> propMap, HttpClientBuilder builder) {
        Map connection = (Map) propMap.get("connection");
        if (connection != null) {
            Integer socketTimeout = getInteger(connection.get("socketTimeout"));
            if (socketTimeout == null) {
                socketTimeout = 5000;
            }
            Integer connectTimeout = getInteger(connection.get("connectTimeout"));
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
        List<Map<String, ?>> servers = (List<Map<String, ?>>) config.get("servers");
        if (servers != null && !servers.isEmpty()) {
            for (Map<String, ?> server : servers) {
                String username = (String) server.get(TaskInputArgs.USER);
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
            Integer port = getInteger(server.get("port"));
            if (!Strings.isNullOrEmpty(host) && port != null) {
                logger.info("Creating the auth scope for host [{}] and port [{}]", host, port);
                return new AuthScope(host, port);
            } else {
                return null;
            }
        }
    }

    protected static Integer getInteger(Object numObj) {
        Integer in = null;
        if (numObj instanceof String) {
            String str = (String) numObj;
            //We want to fail if it is an invalid number
            if (!Strings.isNullOrEmpty(str)) {
                in = Integer.parseInt(str);
            }
        } else if (numObj instanceof Number) {
            in = ((Number) numObj).intValue();
        }
        if (numObj == null) {

        }
        return in;
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

    public static String getPassword(Map<String, ?> propMap, Map<String, ?> config) {
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
            String[] sslProtocols = asStringArray(connection.get("sslProtocols"));
            logger.info("The supported ssl protocols are {}", sslProtocols != null ? Arrays.toString(sslProtocols) : "default");
            String[] sslCipherSuites = asStringArray(connection.get("sslCipherSuites"));
            logger.info("The supported ssl cipher suites are {}", sslCipherSuites != null ? Arrays.toString(sslCipherSuites) : "default");
            Boolean sslCertCheckEnabled = getBoolean(connection.get("sslCertCheckEnabled"));
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
                KeyStore keyStore = loadDefaultTrustStore(propMap);
                try {
                    SSLContext sslContext = SSLContexts.custom()
                            .loadTrustMaterial(keyStore).build();
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

    protected static Boolean getBoolean(Object bool) {
        if (bool instanceof Boolean) {
            return (Boolean) bool;
        } else if (bool instanceof String) {
            String boolStr = (String) bool;
            if (!Strings.isNullOrEmpty(boolStr)) {
                return Boolean.parseBoolean(boolStr);
            }
        }
        return null;
    }

    protected static KeyStore loadDefaultTrustStore(Map<String, ?> propMap) {
        Map<String, ?> connection = (Map<String, ?>) propMap.get("connection");
        File file = resolveTrustStorePath(connection);
        if (file != null && file.exists()) {
            try {
                logger.debug("Loading the keystore from [{}]", file.getAbsolutePath());
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

    protected static char[] getTrustStorePassword(Map<String, ?> propMap, Map<String, ?> connection) {
        String sslTrustStorePassword = (String) connection.get("sslTrustStorePassword");
        if (!Strings.isNullOrEmpty(sslTrustStorePassword)) {
            return sslTrustStorePassword.toCharArray();
        } else {
            String sslTrustStorePasswordEncrypted = (String) connection.get("sslTrustStorePasswordEncrypted");
            String encryptionKey = getEncryptionKey(propMap);
            if (!Strings.isNullOrEmpty(sslTrustStorePasswordEncrypted) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(sslTrustStorePasswordEncrypted).toCharArray();
            } else {
                logger.warn("Returning null password for sslTrustStore. Please set the [connection.sslTrustStorePassword] or " +
                        "[connection.sslTrustStorePasswordEncrypted + encryptionKey]");
                return null;
            }
        }
    }

    private static Object getPath(File file) {
        return file != null ? file.getAbsolutePath() : null;
    }

    protected static String[] asStringArray(Object value) {
        if (value instanceof List) {
            List<String> values = (List) value;
            if (!values.isEmpty()) {
                return values.toArray(new String[values.size()]);
            }
        } else if (value instanceof String) {
            String val = (String) value;
            if (!Strings.isNullOrEmpty(val)) {
                return val.trim().split(",");
            }
        }
        return null;
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
}
