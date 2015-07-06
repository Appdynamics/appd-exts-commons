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
        return startSSL(port, new HelloHandler());
    }
    public static Server startSSL(int port, Handler handler) {
        SslContextFactory factory = new SslContextFactory();
        factory.setKeyStoreResource(Resource.newClassPathResource("/keystore/keystore.jks"));
        factory.setKeyStorePassword("changeit");
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

    public static class AuthenticatedHandler extends AbstractHandler {

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {
            String authorization = request.getHeader("Authorization");
            if (!Strings.isNullOrEmpty(authorization)) {
                logger.info("The Auth Header is {}",authorization);
                String userPass = new String(Base64.decodeBase64(authorization.replace("Basic ", "")));
                if ("user:welcome".equals(userPass)) {
                    response.setStatus(200);
                } else {
                    response.setStatus(401);
                }
            } else {
                response.setStatus(401);
                logger.info("Auth not present, requesting authentication");
                response.setHeader("WWW-Authenticate","Basic realm=\"Mock Test\"");
            }
            ServletOutputStream out = response.getOutputStream();
            out.write("hello from server".getBytes());
            out.flush();
            out.close();
        }
    }

    public static void main(String[] args) {
        MockJettyServer.start(5550, new AuthenticatedHandler());
    }
}
