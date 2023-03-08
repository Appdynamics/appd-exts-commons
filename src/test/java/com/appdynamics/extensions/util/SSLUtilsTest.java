package com.appdynamics.extensions.util;

import com.google.common.collect.Maps;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Map;

import static com.appdynamics.extensions.SystemPropertyConstants.KEYSTORE_PATH_PROPERTY;
import static com.appdynamics.extensions.SystemPropertyConstants.TRUSTSTORE_PATH_PROPERTY;

/**
 * Created by venkata.konala on 1/23/19.
 */
public class SSLUtilsTest {

    @Test
    public void whenNoProtocolSpecifiedShouldSupportTLStest() throws Exception {
        int port = 8900;
        Server server = MockJettyServer.startSSL(port, "/keystore/keystore.jks");
        Map<String, Object> propMap = Maps.newHashMap();
        Map<String, Object> connectionMap = Maps.newHashMap();
        connectionMap.put("sslTrustStorePath", "src/test/resources/keystore/truststore.jks");
        connectionMap.put("sslTrustStorePassword", "changeit");
        propMap.put("connection", connectionMap);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setSSLContext(SSLUtils.createSSLContext(null, propMap));
        httpClientBuilder.setSSLHostnameVerifier(new SSLUtils.AllHostnameVerifier());
        CloseableHttpClient client = httpClientBuilder.build();
        HttpGet get = new HttpGet("https://localhost:" + port);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        server.stop();
    }

    @Test
    public void whenKeyStorePasswordProvidedBuildKeyStoreTest() throws Exception{
        Map<String, String> connection = Maps.newHashMap();
        connection.put("sslKeyStorePath", "src/test/resources/keystore/new-keystore.jks");
        connection.put("sslKeyStorePassword", "test1234");
        KeyStore keyStore = SSLUtils.buildKeyStore(null, connection, null);
        Assert.assertTrue(keyStore.containsAlias("caroot"));
        Assert.assertTrue(keyStore.containsAlias("caroot2"));
        Assert.assertTrue(keyStore.containsAlias("caroot3"));
        Assert.assertTrue(keyStore.containsAlias("kafka"));
    }

    @Test
    public void whenEncryptedKeyStorePasswordProvidedBuildKeyStoreTest() throws Exception{
        Map<String, String> connection = Maps.newHashMap();
        connection.put("sslKeyStorePath", "src/test/resources/keystore/new-keystore.jks");
        connection.put("sslKeyStoreEncryptedPassword", "2SpnWO8RXDq39v2UU329sg==");
        KeyStore keyStore = SSLUtils.buildKeyStore(null, connection, "encryptionKey");
        Assert.assertTrue(keyStore.containsAlias("caroot"));
        Assert.assertTrue(keyStore.containsAlias("caroot2"));
        Assert.assertTrue(keyStore.containsAlias("caroot3"));
        Assert.assertTrue(keyStore.containsAlias("kafka"));
    }

    @Test
    public void whenKeyStorePathProvidedBuildKeyStoreTest() throws Exception{
        Map<String, String> connection = Collections.singletonMap("sslKeyStorePath", "src/test/resources/keystore/new-keystore.jks");
        KeyStore keyStore = SSLUtils.buildKeyStore(null, connection, null);
        Assert.assertTrue(keyStore.containsAlias("caroot"));
        Assert.assertTrue(keyStore.containsAlias("caroot2"));
        Assert.assertTrue(keyStore.containsAlias("caroot3"));
        Assert.assertTrue(keyStore.containsAlias("kafka"));
    }

    @Test
    public void whenKeyStorePathNotProvidedBuildKeyStoreFromSysPropTest() throws Exception{
        System.setProperty(KEYSTORE_PATH_PROPERTY, "src/test/resources/keystore/new-keystore.jks");
        KeyStore keyStore = SSLUtils.buildKeyStore(null, null, null);
        Assert.assertTrue(keyStore.containsAlias("caroot"));
        Assert.assertTrue(keyStore.containsAlias("caroot2"));
        Assert.assertTrue(keyStore.containsAlias("caroot3"));
        Assert.assertTrue(keyStore.containsAlias("kafka"));
        System.clearProperty(KEYSTORE_PATH_PROPERTY);
    }

    @Test
    public void whenKeyStorePathNotSpecifiedReturnNullTest() throws Exception{
        KeyStore keyStore = SSLUtils.buildKeyStore(null, null, null);
        Assert.assertTrue(keyStore == null);
    }

    @Test
    public void whenTrustStorePasswordProvidedBuildTrustStoreTest() throws Exception{
        Map<String, String> connection = Maps.newHashMap();
        connection.put("sslTrustStorePath", "src/test/resources/keystore/new-truststore.jks");
        connection.put("sslTrustStorePassword", "test1234");
        KeyStore trustStore = SSLUtils.buildTrustStore(null, connection, null);
        Assert.assertTrue(trustStore.containsAlias("caroot"));
        Assert.assertTrue(trustStore.containsAlias("caroot2"));
        Assert.assertTrue(trustStore.containsAlias("caroot3"));
        Assert.assertTrue(trustStore.containsAlias("kafka"));
    }

    @Test
    public void whenEncryptedTrustStorePasswordProvidedBuildTrustStoreTest() throws Exception{
        Map<String, String> connection = Maps.newHashMap();
        connection.put("sslTrustStorePath", "src/test/resources/keystore/new-truststore.jks");
        connection.put("sslTrustStoreEncryptedPassword", "2SpnWO8RXDq39v2UU329sg==");
        KeyStore trustStore = SSLUtils.buildTrustStore(null, connection, "encryptionKey");
        Assert.assertTrue(trustStore.containsAlias("caroot"));
        Assert.assertTrue(trustStore.containsAlias("caroot2"));
        Assert.assertTrue(trustStore.containsAlias("caroot3"));
        Assert.assertTrue(trustStore.containsAlias("kafka"));
    }

    @Test
    public void whenTrustStorePathProvidedBuildKeyStoreTest() throws Exception{
        Map<String, String> connection = Collections.singletonMap("sslTrustStorePath", "src/test/resources/keystore/new-truststore.jks");
        KeyStore keyStore = SSLUtils.buildTrustStore(null, connection, null);
        Assert.assertTrue(keyStore.containsAlias("caroot"));
        Assert.assertTrue(keyStore.containsAlias("caroot2"));
        Assert.assertTrue(keyStore.containsAlias("caroot3"));
        Assert.assertTrue(keyStore.containsAlias("kafka"));
    }

    @Test
    public void whenTrustStorePathNotProvidedBuildKeyStoreFromSysPropTest() throws Exception{
        System.setProperty(TRUSTSTORE_PATH_PROPERTY, "src/test/resources/keystore/new-truststore.jks");
        KeyStore keyStore = SSLUtils.buildTrustStore(null, null, null);
        Assert.assertTrue(keyStore.containsAlias("caroot"));
        Assert.assertTrue(keyStore.containsAlias("caroot2"));
        Assert.assertTrue(keyStore.containsAlias("caroot3"));
        Assert.assertTrue(keyStore.containsAlias("kafka"));
        System.clearProperty(TRUSTSTORE_PATH_PROPERTY);
    }

    @Test
    public void whenTrustStorePathNotSpecifiedReturnNullTest() throws Exception{
        KeyStore keyStore = SSLUtils.buildTrustStore(null, null, null);
        Assert.assertTrue(keyStore == null);
    }
}
