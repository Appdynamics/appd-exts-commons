package com.appdynamics.extensions.util;

import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by abey.tom on 6/30/15.
 */
public class MockJettyServer {
    public static final Logger logger = LoggerFactory.getLogger(MockJettyServer.class);

    public static Server start(int port) {
        return start(port, new HelloHandler());
    }

    public static Server start(int port, Handler handler) {
        SelectChannelConnector sslConnector = new SelectChannelConnector();
        sslConnector.setPort(port);
        final Server server = new Server();
        server.setConnectors(new Connector[]{sslConnector});
        server.setHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    public static Server startSSL(int port) {
        return startSSL(port,null);
    }
    public static Server startSSL(int port, String protocol) {
        return startSSL(port, protocol,new HelloHandler());
    }

    public static Server startSSL(int port, String protocol, Handler handler) {
        SslContextFactory factory = new SslContextFactory();
        if(protocol!=null){
            factory.setIncludeProtocols(protocol);
        }
        factory.setKeyStoreResource(Resource.newClassPathResource("/keystore/keystore.jks"));
        factory.setKeyStorePassword("changeit");
        SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(factory);
        sslConnector.setPort(port);
        final Server server = new Server();
        server.setConnectors(new Connector[]{sslConnector});
        server.setHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    public static Server startSSLWithMutualAuth(int port, String protocol, String keyStorePath, String keyStoreType, String trustStorePath, String trustStoreType){
        SslContextFactory factory = new SslContextFactory();
        if(protocol!=null){
            factory.setIncludeProtocols(protocol);
        }
        if(keyStorePath != null){
            factory.setKeyStoreResource(Resource.newClassPathResource(keyStorePath));
            factory.setKeyStoreType(keyStoreType);
            factory.setKeyStorePassword("changeit");
        }
        if(trustStorePath != null){
            factory.setTrustStoreResource(Resource.newClassPathResource(trustStorePath));
            factory.setTrustStoreType(trustStoreType);
            factory.setTrustStorePassword("changeit");
        }


        factory.setNeedClientAuth(true);
        SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(factory);
        sslConnector.setPort(port);
        final Server server = new Server();
        server.setConnectors(new Connector[]{sslConnector});
        server.setHandler(new HelloHandler());
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    private static class HelloHandler extends AbstractHandler {

        private HelloHandler() {
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {
            response.setStatus(200);
            ServletOutputStream out = response.getOutputStream();
            out.write("hello from server".getBytes());
            out.flush();
            out.close();
        }
    }

    public static class ProxyHandler extends AbstractHandler {
        private boolean authenticate;

        public ProxyHandler(boolean authenticate) {
            this.authenticate = authenticate;
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request
                , HttpServletResponse response) throws IOException, ServletException {
            PrintWriter writer = response.getWriter();
            if(authenticate){
                String header = request.getHeader("Proxy-Authorization");
                if(Strings.isNullOrEmpty(header)){
                    response.setStatus(407);
                    response.setHeader("Proxy-Authenticate","Basic realm=\"proxy.com\"");
                } else{
                    String value = header.replace("Basic ", "");
                    String s = new String(Base64.decodeBase64(value));
                    if(s.equals("proxyuser:proxypassword")){
                        writer.write("AuthSuccess");
                    } else{
                        response.setStatus(407);
                        response.setHeader("Proxy-Authenticate","Basic realm=\"proxy.com\"");
                    }
                }
            } else{
                writer.write("NoAuth");
            }
            baseRequest.setHandled(true);
            writer.close();
        }
    }

    public static class AuthenticatedHandler extends AbstractHandler {

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {
            String authorization = request.getHeader("Authorization");
            if (!Strings.isNullOrEmpty(authorization)) {
                logger.info("The Auth Header is {}", authorization);
                String userPass = new String(Base64.decodeBase64(authorization.replace("Basic ", "")));
                if ("user:welcome".equals(userPass)) {
                    response.setStatus(200);
                } else {
                    response.setStatus(401);
                }
            } else {
                response.setStatus(401);
                logger.info("Auth not present, requesting authentication");
                response.setHeader("WWW-Authenticate", "Basic realm=\"Mock Test\"");
            }
            ServletOutputStream out = response.getOutputStream();
            out.write("hello from server".getBytes());
            out.flush();
            out.close();
        }
    }

    public static void main(String[] args) {
    }
}
