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

package com.appdynamics.extensions.http;

import com.appdynamics.extensions.util.MockJettyServer;
import com.appdynamics.extensions.util.YmlUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.util.*;

/**
 * Created by abey.tom on 6/30/15.
 * Created by abey.tom on 6/30/15.
 */
public class Http4ClientBuilderTest {

    public static final String PROXY_URI = "http://localhost:4823";
    public static final String PROXYUSER = "proxyuser";
    public static final String PROXYPASSWORD = "proxypassword";

    @Test(expected = javax.net.ssl.SSLHandshakeException.class)
    public void testConfigureSSL() throws Exception {
        int port = 8755;
        Server server = MockJettyServer.startSSL(port);
        Map<String, ?> map = Collections.emptyMap();
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("https://localhost:" + port);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        server.stop();
    }

    @Test
    public void testConfigureSSLWithSSLChecksDisabled() throws Exception {
        int port = 8756;
        Server server = MockJettyServer.startSSL(port);
        Map<String, ?> map = Collections.singletonMap("sslCertCheckEnabled", false);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(Collections.singletonMap("connection", map));
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("https://localhost:" + port);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        server.stop();
    }


    @Test
    public void testConfigureProxyWithOutCred() throws Exception {
        Server server = MockJettyServer.start(4823, new MockJettyServer.ProxyHandler(true));
        Map map = new HashMap();
        HashMap<String, String> proxyProps = new HashMap<String, String>();
        map.put("proxy", proxyProps);
        proxyProps.put("uri", PROXY_URI);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("https://www.google.com");
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(407, response.getStatusLine().getStatusCode());
        response.close();
        server.stop();
    }

    @Test
    public void testConfigureProxyWithCred() throws Exception {
        Server server = MockJettyServer.start(4823, new MockJettyServer.ProxyHandler(true));
        Map map = new HashMap();
        HashMap<String, String> proxyProps = new HashMap<String, String>();
        map.put("proxy", proxyProps);
        proxyProps.put("uri", PROXY_URI);
        proxyProps.put("username", PROXYUSER);
        proxyProps.put("password", PROXYPASSWORD);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("http://www.google.com");
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        response.close();
        server.stop();
    }

    @Test
    public void testConfigureProxyWithWrongCred() throws Exception {
        Server server = MockJettyServer.start(4823, new MockJettyServer.ProxyHandler(true));
        Map map = new HashMap();
        HashMap<String, String> proxyProps = new HashMap<String, String>();
        map.put("proxy", proxyProps);
        proxyProps.put("uri", PROXY_URI);
        proxyProps.put("username", PROXYUSER);
        proxyProps.put("password", PROXYPASSWORD + "1");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("https://www.google.com");
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(407, response.getStatusLine().getStatusCode());
        response.close();
        server.stop();
    }

    @Test
    public void testConfigureAuthenticationWithoutPassword() throws Exception {
        int port = 8757;
        Server jetty = MockJettyServer.start(port, new MockJettyServer.AuthenticatedHandler());
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", "http://localhost:" + port);
        list.add(server);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("http://localhost:" + port);
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
        response.close();
        jetty.stop();
    }

    @Test
    public void testConfigureAuthenticationWithPassword() throws Exception {
        int port = 8758;
        Server jetty = MockJettyServer.start(port, new MockJettyServer.AuthenticatedHandler());
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", "http://localhost:" + port);
        server.put("username", "user");
        server.put("password", "welcome");
        list.add(server);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("http://localhost:" + port);
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        response.close();
        jetty.stop();
    }

    @Test
    public void testConfigureAuthenticationWithWrongPassword() throws Exception {
        final int port = 8721;
        Server jetty = MockJettyServer.start(port, new MockJettyServer.AuthenticatedHandler());
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", "http://localhost:" + port + "/test/hello/abey");
        server.put("username", "user");
        server.put("password", "welcome1");
        list.add(server);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                HttpPost post = new HttpPost("http://localhost:" + port + "/test/hello/abey");
                post.setEntity(new StringEntity("Test", ContentType.APPLICATION_XML));
                try {
                    client.execute(post, new ResponseHandler<HttpResponse>() {
                        public HttpResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                            Assert.assertEquals(401, response.getStatusLine().getStatusCode());
                            return response;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        t1.join();
        jetty.stop();
    }

    @Test(expected = SSLHandshakeException.class)
    public void testWithIncorrectSslProtocol() throws Exception {
        final int port = 8729;
        String uri = "https://localhost:" + port + "/test/hello/abey";

        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        Server jetty = MockJettyServer.startSSL(port, "TLSv1.2");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();

    }

    @Test
    public void testWithCorrectSslProtocolWithoutHostNameVerify() throws Exception {
        final int port = 8723;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", false);
        connection.put("sslTrustStorePath", "src/test/resources/keystore/keystore.jks");
       // connection.put("sslTrustStorePassword", "changeit");
        Server jetty = MockJettyServer.startSSL(port, "TLSv1.1");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();

    }

    @Test(expected = javax.net.ssl.SSLHandshakeException.class)
    public void testWithCorrectSslProtocolWithoutHostNameVerifyWithWrongPassword() throws Exception {
        final int port = 8759;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", false);
        connection.put("sslTrustStorePassword", "dontchangeit");
        connection.put("sslTrustStorePath", "src/test/resources/keystore/keystore.jks");
        Server jetty = MockJettyServer.startSSL(port, "TLSv1.1");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();

    }

    @Test
    public void testStringArray() {
        String[] protocols = YmlUtils.asStringArray(Arrays.asList("TLS", "SSL"));
        Assert.assertEquals("[TLS, SSL]", Arrays.toString(protocols));
        protocols = YmlUtils.asStringArray("TLS,SSL");
        Assert.assertEquals("[TLS, SSL]", Arrays.toString(protocols));
        protocols = YmlUtils.asStringArray(Collections.emptyList());
        Assert.assertNull(protocols);
        protocols = YmlUtils.asStringArray("");
        Assert.assertNull(protocols);
        protocols = YmlUtils.asStringArray(null);
        Assert.assertNull(protocols);


    }

    @Test
    public void testGetInteger() {
        Integer integer = YmlUtils.getInteger(10.01D);
        Assert.assertEquals(10, (int) integer);
        integer = YmlUtils.getInteger("10");
        Assert.assertEquals(10, (int) integer);
        integer = YmlUtils.getInteger("");
        Assert.assertNull(integer);
        integer = YmlUtils.getInteger(null);
        Assert.assertNull(integer);

    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntegerInvalid() {
        Integer integer = YmlUtils.getInteger("a");
        Assert.assertNull(integer);
    }

    @Test
    public void testGetBoolean() {
        Boolean aBoolean = YmlUtils.getBoolean(Boolean.TRUE);
        Assert.assertTrue(aBoolean);
        aBoolean = YmlUtils.getBoolean("true");
        Assert.assertTrue(aBoolean);
        aBoolean = YmlUtils.getBoolean("false");
        Assert.assertFalse(aBoolean);
        aBoolean = YmlUtils.getBoolean("abcd");
        Assert.assertFalse(aBoolean);
        aBoolean = YmlUtils.getBoolean("");
        Assert.assertNull(aBoolean);
        aBoolean = YmlUtils.getBoolean(null);
        Assert.assertNull(aBoolean);
    }


    /*
        keytool -genkey -keyalg RSA -alias appd-extensions -keystore selfsigned.jks -validity 2000 -keysize 2048

        keytool -list -v -alias appd-extensions -keystore selfsigned.jks

        keytool -export -alias appd-extensions -file client.cer -keystore selfsigned.jks

        keytool -import -v -alias extensions-client -file client.cer -keystore client-keystore.jks
     */
    @Test
    public void whenSSLWithMutualAuthWithProperClientCertsAndNoHostnameVerificationThenAuthenticateSuccessfully() throws Exception {
        int port = 8768;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", false);
        connection.put("sslKeyStorePassword","changeit");
        connection.put("sslTrustStorePassword","changeit");
        connection.put("sslTrustStorePath", "src/test/resources/clientCertWithHttps/nohostverify/server.jks");
        connection.put("sslKeyStorePath","src/test/resources/clientCertWithHttps/nohostverify/client.jks");
        Server jetty = MockJettyServer.startSSLWithMutualAuth(port, "TLSv1.1", "/clientCertWithHttps/nohostverify/server.jks","JKS","/clientCertWithHttps/nohostverify/client.jks","JKS");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();
    }


    @Test(expected = java.net.SocketException.class)
    public void whenSSLWithMutualAuthAndIncorrectClientCertsAndNoHostnameVerificationThenAuthenticateFailure() throws Exception {
        int port = 8775;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", false);
        connection.put("sslKeyStorePassword","changeit");
        connection.put("sslTrustStorePassword","changeit");
        connection.put("sslTrustStorePath", "src/test/resources/clientCertWithHttps/nohostverify/server.jks");
        connection.put("sslKeyStorePath","src/test/resources/keystore/keystore.jks");
        Server jetty = MockJettyServer.startSSLWithMutualAuth(port, "TLSv1.1", "/clientCertWithHttps/nohostverify/server.jks","JKS","/clientCertWithHttps/nohostverify/client.jks","JKS");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();
    }

    @Test(expected = javax.net.ssl.SSLHandshakeException.class)
    public void whenSSLWithMutualAuthAndNoPasswordAndNoHostnameVerificationThenAuthenticateFailure() throws Exception {
        int port = 8769;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", false);
        connection.put("sslTrustStorePassword","changeit");
        connection.put("sslTrustStorePath", "src/test/resources/clientCertWithHttps/nohostverify/server.jks");
        connection.put("sslKeyStorePath","src/test/resources/keystore/keystore.jks");
        Server jetty = MockJettyServer.startSSLWithMutualAuth(port, "TLSv1.1", "/clientCertWithHttps/nohostverify/server.jks","JKS","/clientCertWithHttps/nohostverify/client.jks","JKS");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();
    }


    @Test(expected = java.net.SocketException.class)
    public void whenSSLWithMutualAuthWithNoClientCertsAndNoHostnameVerificationThenAuthenticateFailure() throws Exception {
        int port = 8680;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", false);
        connection.put("sslKeyStorePassword","changeit");
        connection.put("sslTrustStorePassword","changeit");
        connection.put("sslTrustStorePath", "src/test/resources/clientCertWithHttps/nohostverify/server.jks");
        Server jetty = MockJettyServer.startSSLWithMutualAuth(port, "TLSv1.1", "/clientCertWithHttps/nohostverify/server.jks","JKS","/clientCertWithHttps/nohostverify/client.jks","JKS");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();
    }



    @Test
    public void whenSSLWithMutualAuthWithProperClientCertsAndWithProperHostnameThenAuthenticateSuccessfully() throws Exception{
        int port = 8670;
        String uri = "https://localhost:" + port + "/test/hello/abey";
        Map map = new HashMap();
        List list = new ArrayList();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", uri);
        Map connection = new HashMap();
        map.put("connection", connection);
        connection.put("sslProtocols", "TLSv1.1");
        connection.put("sslVerifyHostname", true);
        connection.put("sslKeyStorePassword","changeit");
        connection.put("sslTrustStorePassword","changeit");
        connection.put("sslTrustStorePath", "src/test/resources/clientCertWithHttps/hostverify/server.jks");
        connection.put("sslKeyStorePath","src/test/resources/clientCertWithHttps/hostverify/client.jks");
        Server jetty = MockJettyServer.startSSLWithMutualAuth(port, "TLSv1.1", "/clientCertWithHttps/hostverify/server.jks","JKS","/clientCertWithHttps/hostverify/client.jks","JKS");
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        final CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        response.close();
        jetty.stop();
    }
}