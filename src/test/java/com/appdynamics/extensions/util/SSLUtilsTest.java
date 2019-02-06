package com.appdynamics.extensions.util;

import com.google.common.collect.Maps;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

import java.util.Map;

/**
 * Created by venkata.konala on 1/23/19.
 */
public class SSLUtilsTest {

    @Test
    public void testConfigureSSL() throws Exception {
        int port = 8900;
        Server server = MockJettyServer.startSSL(port, "TLSv1.2", "/keystore/keystore.jks");
        Map<String, Object> propMap = Maps.newHashMap();
        Map<String, Object> connectionMap = Maps.newHashMap();
        connectionMap.put("sslTrustStorePath", "src/test/resources/keystore/keystore.jks");
        connectionMap.put("sslTrustStorePassword", "changeit");
        //connectionMap.put("sslProtocols", Arrays.asList("TLSv1.2"));
        propMap.put("connection", connectionMap);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setSslcontext(SSLUtils.createSSLContext(null, propMap));
        httpClientBuilder.setHostnameVerifier(new SSLUtils.AllHostnameVerifier());
        CloseableHttpClient client = httpClientBuilder.build();
        HttpGet get = new HttpGet("https://localhost:" + port);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        server.stop();
    }

    /*@Test
    public void resolveTrustStorePath() {
        Map<String, String> connection = Collections.singletonMap("sslTrustStorePath", "src/test/resources/keystore/truststore.changethis.jks");
        File file = Http4ClientBuilder.resolveTrustStorePath(connection);
        Assert.assertEquals("truststore.changethis.jks", file.getName());

        System.setProperty(TRUSTSTORE_PATH_PROPERTY, "src/test/resources/keystore/keystore.jks");
        file = Http4ClientBuilder.resolveTrustStorePath(connection);
        Assert.assertEquals("keystore.jks", file.getName());
        System.getProperties().remove(TRUSTSTORE_PATH_PROPERTY);
    }

    @Test
    public void getTrustStorePassword() {
        Map propMap = new HashMap();

        Map connection = new HashMap();
        propMap.put("connection", connection);
        String encrypted = new Encryptor("welcome").encrypt("welcome");
        //No Pass
        char[] pwd = Http4ClientBuilder.getTrustStorePassword(propMap, connection);
        Assert.assertNull(pwd);

        // Enc Pass without password
        connection.put("sslTrustStoreEncryptedPassword", encrypted);
        pwd = Http4ClientBuilder.getTrustStorePassword(propMap, connection);
        Assert.assertNull(pwd);

        //With enc key
        propMap.put("encryptionKey", "welcome");
        pwd = Http4ClientBuilder.getTrustStorePassword(propMap, connection);
        Assert.assertEquals("welcome", new String(pwd));

        connection.put("sslTrustStorePassword", "welcome1");
        pwd = Http4ClientBuilder.getTrustStorePassword(propMap, connection);
        Assert.assertEquals("welcome1", new String(pwd));
    }

    @Test
    public void resolveKeyStorePath() {
        Map<String, String> connection = Collections.singletonMap("sslKeyStorePath", "src/test/resources/clientCertWithHttps/hostverify/server.jks");
        File file = Http4ClientBuilder.resolveKeyStorePath(connection);
        Assert.assertEquals("server.jks", file.getName());

        System.setProperty(KEYSTORE_PATH_PROPERTY, "src/test/resources/keystore/keystore.jks");
        file = Http4ClientBuilder.resolveKeyStorePath(connection);
        Assert.assertEquals("keystore.jks", file.getName());
        System.getProperties().remove(KEYSTORE_PATH_PROPERTY);
    }

    @Test
    public void getKeyStorePassword() {
        Map propMap = new HashMap();

        Map connection = new HashMap();
        propMap.put("connection", connection);
        String encrypted = new Encryptor("welcome").encrypt("welcome");
        //No Pass
        char[] pwd = Http4ClientBuilder.getKeyStorePassword(propMap, connection);
        Assert.assertNull(pwd);

        // Enc Pass without password
        connection.put("sslKeyStoreEncryptedPassword", encrypted);
        pwd = Http4ClientBuilder.getKeyStorePassword(propMap, connection);
        Assert.assertNull(pwd);

        //With enc key
        propMap.put("encryptionKey", "welcome");
        pwd = Http4ClientBuilder.getKeyStorePassword(propMap, connection);
        Assert.assertEquals("welcome", new String(pwd));

        connection.put("sslKeyStorePassword", "welcome1");
        pwd = Http4ClientBuilder.getKeyStorePassword(propMap, connection);
        Assert.assertEquals("welcome1", new String(pwd));
    }*/
}
