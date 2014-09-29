package com.appdynamics.extensions.http;

import com.appdynamics.TaskInputArgs;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/8/14
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleHttpClientTest {
    public static final Logger logger = LoggerFactory.getLogger(SimpleHttpClientTest.class);

    public static final String DEFAULT_USER = "user1";
    public static final String DEFAULT_PWD = "welcome";
    public static final String DEFAULT_HTTP_URL = "http://localhost:8654";
    public static final String DEFAULT_HTTPS_URL = "https://localhost:8754";
    public static final String LOCALHOST = "localhost";
    public static final String DEFAULT_PORT = "8654";
    public static final String DEFAULT_SSL_PORT = "8754";
    private DelegateHandler delegateHandler;
    private Server server;


    @Before
    public void setUp() throws Exception {
        delegateHandler = new DelegateHandler();
        server = startJettySSl(delegateHandler);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

//    @Test
    //THis requires a fwd proxy to be running
    public void testProxy() {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.URI, "http://www.google.com");
        taskArgs.put(TaskInputArgs.PROXY_HOST, "localhost");
        taskArgs.put(TaskInputArgs.PROXY_PORT, "80");
        taskArgs.put(TaskInputArgs.PROXY_USER, "username");
        taskArgs.put(TaskInputArgs.PROXY_PASSWORD, "welcome");
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        String out = client.get().string();
    }

    @Test
    //THis requires a fwd proxy to be running
    public void testProxyWithEncryptedPassword() {
//        Map<String, String> taskArgs = new HashMap<String, String>();
//        taskArgs.put(TaskInputArgs.URI, "http://www.google.com");
//        taskArgs.put(TaskInputArgs.PROXY_HOST, "localhost");
//        taskArgs.put(TaskInputArgs.PROXY_PORT, "80");
//        taskArgs.put(TaskInputArgs.PROXY_USER, "username");
//        taskArgs.put(TaskInputArgs.PROXY_PASSWORD_ENCRYPTED, Encryptor.getInstance().crypto("welcome"));
//        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
//        String out = client.get().string();
    }

    @Test
    public void testPostMethodWithData(){
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USER, DEFAULT_USER);
        taskArgs.put(TaskInputArgs.PASSWORD, DEFAULT_PWD);
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        delegateHandler.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                String actual = request.getHeader("Authorization");
                String expected = "Basic " + new String(Base64.encodeBase64((DEFAULT_USER + ":" + DEFAULT_PWD).getBytes()), "ASCII");
                logger.info("The expected header was '{}' and the actual is '{}'", expected, actual);
                Assert.assertEquals(expected, actual);
                Assert.assertEquals("POST",request.getMethod());
                ServletInputStream in = request.getInputStream();
                byte[] b = new byte[8];
                in.read(b);
                Assert.assertEquals("POSTDATA",new String(b));
            }
        });
        Response response = client.target().uri(DEFAULT_HTTP_URL).post("POSTDATA");
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

    @Test
    public void testHeaderPostMethodWithData(){
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USER, DEFAULT_USER);
        taskArgs.put(TaskInputArgs.PASSWORD, DEFAULT_PWD);
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        delegateHandler.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                String actual = request.getHeader("Authorization");
                String expected = "Basic " + new String(Base64.encodeBase64((DEFAULT_USER + ":" + DEFAULT_PWD).getBytes()), "ASCII");
                logger.info("The expected header was '{}' and the actual is '{}'", expected, actual);
                Assert.assertEquals(expected, actual);
                Assert.assertEquals("POST",request.getMethod());
                ServletInputStream in = request.getInputStream();
                byte[] b = new byte[8];
                in.read(b);
                Assert.assertEquals("POSTDATA",new String(b));
                Assert.assertEquals("value",request.getHeader("key"));
            }
        });
        Response response = client.target().uri(DEFAULT_HTTP_URL).header("key","value").post("POSTDATA");
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

    @Test
    public void testAuthenticationDefault() {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USER, DEFAULT_USER);
        taskArgs.put(TaskInputArgs.PASSWORD, DEFAULT_PWD);
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        delegateHandler.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                String actual = request.getHeader("Authorization");
                String expected = "Basic " + new String(Base64.encodeBase64((DEFAULT_USER + ":" + DEFAULT_PWD).getBytes()), "ASCII");
                logger.info("The expected header was '{}' and the actual is '{}'", expected, actual);
                Assert.assertEquals(expected, actual);
            }
        });
        Response response = client.target().uri(DEFAULT_HTTP_URL).get();
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

    @Test
    public void testNoAuthentication() {
        Map<String, String> taskArgs = new HashMap<String, String>();
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        delegateHandler.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                String actual = request.getHeader("Authorization");
                logger.info("The Authorization header is {}", actual);
                Assert.assertNull(actual);
            }
        });
        Response response = client.target().uri(DEFAULT_HTTP_URL).get();
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

    @Test
    public void testEmptyPasswordAuthentication() {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USER, DEFAULT_USER);
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        delegateHandler.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                String actual = request.getHeader("Authorization");
                String expected = "Basic " + new String(Base64.encodeBase64((DEFAULT_USER + ":").getBytes()), "ASCII");
                logger.info("The expected header was '{}' and the actual is '{}'", expected, actual);
                Assert.assertEquals(expected, actual);
            }
        });
        Response response = client.target().uri(DEFAULT_HTTP_URL).get();
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

    @Test
    public void testSSLWithHostPort() throws IOException {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USE_SSL, "true");
        taskArgs.put(TaskInputArgs.HOST, "localhost");
        taskArgs.put(TaskInputArgs.PORT, DEFAULT_SSL_PORT);
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        Response response = client.target().uri(DEFAULT_HTTPS_URL + "/test?key=value&key=value3#test/abey").get();
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

    @Test
    public void testSSLTrustStoreDefaultPwdAllHostsVerifier() throws IOException {
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USE_SSL, "true");
        taskArgs.put(TaskInputArgs.URI, DEFAULT_HTTPS_URL);
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        Response response = client.target().uri(DEFAULT_HTTPS_URL + "/test?key=value&key=value3#test/abey").get();
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }

   /* *//**
     * Sets the encrypted password and the http client should decrypt and sent it over the wire.
     *//*
    @Test
    public void testPasswordEncryption(){
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(TaskInputArgs.USER, DEFAULT_USER);
        Encryptor encryptor = new Encryptor("hello");
        taskArgs.put(TaskInputArgs.PASSWORD_ENCRYPTED, encryptor.encrypt("password".toCharArray()));
        SimpleHttpClient client = SimpleHttpClient.builder(taskArgs).build();
        delegateHandler.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                String actual = request.getHeader("Authorization");
                String expected = "Basic " + new String(Base64.encodeBase64((DEFAULT_USER + ":password").getBytes()), "ASCII");
                logger.info("The expected header was '{}' and the actual is '{}'", expected, actual);
                Assert.assertEquals(expected, actual);
            }
        });
        Response response = client.target().uri(DEFAULT_HTTP_URL).get();
        Assert.assertEquals(200,response.getStatus());
        logger.info(response.string());
    }*/

    private Server startJettySSl(final Handler handler) {
        SslContextFactory factory = new SslContextFactory();
        factory.setKeyStoreResource(Resource.newClassPathResource("/keystore/keystore.jks"));
        factory.setKeyStorePassword("changeit");
        SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(factory);
        sslConnector.setPort(8754);
        SocketConnector connector = new SocketConnector();
        connector.setPort(8654);
        final Server server = new Server();
        server.setConnectors(new Connector[]{sslConnector, connector});
        server.setHandler(handler);
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                try {
                    server.start();
                    latch.countDown();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return server;
    }

    private static class DelegateHandler extends AbstractHandler {
        public Handler handler;

        private DelegateHandler() {
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {
            if (handler != null) {
                handler.handle(target, baseRequest, request, response);
            }
            response.setStatus(200);
            ServletOutputStream out = response.getOutputStream();
            out.write("complete".getBytes());
            out.flush();
            out.close();
        }

        private void setHandler(Handler handler) {
            this.handler = handler;
        }
    }
}
