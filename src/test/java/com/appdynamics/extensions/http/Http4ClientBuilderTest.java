package com.appdynamics.extensions.http;

import com.appdynamics.extensions.util.MockJettyServer;
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

import java.io.IOException;
import java.util.*;

/**
 * Created by abey.tom on 6/30/15.
 */
public class Http4ClientBuilderTest {

    public static final String PROXY_URI = "http://192.168.1.134:8585";
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
    }

    @Test
    public void testConfigureProxyWithCred() throws Exception {
        Map map = new HashMap();
        HashMap<String, String> proxyProps = new HashMap<String, String>();
        map.put("proxy", proxyProps);
        proxyProps.put("uri", PROXY_URI);
        proxyProps.put("username", PROXYUSER);
        proxyProps.put("password", PROXYPASSWORD);
        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        CloseableHttpClient client = builder.build();
        HttpGet get = new HttpGet("https://www.google.com");
        CloseableHttpResponse response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        response.close();
    }

    @Test
    public void testConfigureProxyWithWrongCred() throws Exception {
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
        final int port = 8759;
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
}