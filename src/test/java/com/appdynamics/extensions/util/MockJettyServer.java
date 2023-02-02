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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;

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
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MockJettyServer.class);

    public static Server start(int port) {
        return start(port, new HelloHandler());
    }

    public static Server start(int port, Handler handler) {
        final Server server = new Server();

        HttpConfiguration http_config = new HttpConfiguration();

        ServerConnector http = new ServerConnector(server,
                new HttpConnectionFactory(http_config));
        http.setPort(port);

        HttpConfiguration https_config = new HttpConfiguration(http_config);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);
        https_config.addCustomizer(src);

        ServerConnector https = new ServerConnector(server,
                new HttpConnectionFactory(https_config));
        https.setPort(port);

        server.setConnectors(new Connector[]{http, https});
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

        SslContextFactory.Server serverSSLContext = new SslContextFactory.Server();
        serverSSLContext.setKeyStoreResource(Resource.newClassPathResource("/keystore/keystore.jks"));
        serverSSLContext.setKeyStorePassword("changeit");
        serverSSLContext.setIncludeProtocols("TLSv1.2","TLSv1.1","TLSv1");
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(serverSSLContext, HttpVersion.HTTP_1_1.asString());

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(port);
        HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);

        final Server server = new Server();

        ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(handler);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    public static Server startSSL(int port, String keyStorePath) {

        SslContextFactory.Server serverSSLContext = new SslContextFactory.Server();
        serverSSLContext.setKeyStoreResource(Resource.newClassPathResource(keyStorePath));
        serverSSLContext.setKeyStorePassword("changeit");
        serverSSLContext.setIncludeProtocols("TLSv1.2","TLSv1.1","TLSv1");

        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(serverSSLContext, HttpVersion.HTTP_1_1.asString());

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(port);
        HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);

        final Server server = new Server();

        ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(new HelloHandler());

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    public static Server startSSLWithMutualAuth(int port, String keyStorePath, String keyStoreType, String trustStorePath, String trustStoreType) {

        SslContextFactory.Server serverSSLContext = new SslContextFactory.Server();

        serverSSLContext.setIncludeProtocols("TLSv1.2","TLSv1.1","TLSv1");

        if (keyStorePath != null) {
            serverSSLContext.setKeyStoreResource(Resource.newClassPathResource(keyStorePath));
            serverSSLContext.setKeyStoreType(keyStoreType);
            serverSSLContext.setKeyStorePassword("changeit");
        }
        if (trustStorePath != null) {
            serverSSLContext.setTrustStoreResource(Resource.newClassPathResource(trustStorePath));
            serverSSLContext.setTrustStoreType(trustStoreType);
            serverSSLContext.setTrustStorePassword("changeit");
        }

        serverSSLContext.setNeedClientAuth(true);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(serverSSLContext, HttpVersion.HTTP_1_1.asString());

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(port);
        HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);

        final Server server = new Server();
        ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        connector.setPort(port);

        server.setConnectors(new Connector[]{connector});
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
            if (authenticate) {
                String header = request.getHeader("Proxy-Authorization");
                if (Strings.isNullOrEmpty(header)) {
                    response.setStatus(407);
                    response.setHeader("Proxy-Authenticate", "Basic realm=\"proxy.com\"");
                } else {
                    String value = header.replace("Basic ", "");
                    String s = new String(Base64.decodeBase64(value));
                    if (s.equals("proxyuser:proxypassword")) {
                        writer.write("AuthSuccess");
                    } else {
                        response.setStatus(407);
                        response.setHeader("Proxy-Authenticate", "Basic realm=\"proxy.com\"");
                    }
                }
            } else {
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
