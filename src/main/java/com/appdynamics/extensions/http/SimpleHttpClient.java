package com.appdynamics.extensions.http;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.NumberUtils;
import com.google.common.base.Strings;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.appdynamics.TaskInputArgs.*;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleHttpClient {
    public static final Logger logger = LoggerFactory.getLogger(SimpleHttpClient.class);
    private final HttpConnectionManager httpConnectionManager;
    private HttpClient httpClient;
    private Map<String, String> taskArgs;
    private JAXBContext jaxbContext;
    protected AuthenticationConfig authConfig;
    protected boolean isSSLSupported;

    public SimpleHttpClient(Map<String, String> taskArgs, JAXBContext jaxbContext, HttpConnectionManagerParams params, boolean multiThreaded) {
        this.taskArgs = taskArgs;
        if (multiThreaded) {
            httpConnectionManager = new MultiThreadedHttpConnectionManager();
        } else {
            httpConnectionManager = new SimpleHttpConnectionManager();
        }
        logger.debug("The HttpConnectionManager is {}", httpConnectionManager);
        httpClient = new HttpClient(httpConnectionManager);
        addProxyConfig();
        authConfig = AuthenticationConfig.build(taskArgs);
        initializeSSL();
        initializeJAXBContext(jaxbContext);
        if (params != null) {
            httpClient.getHttpConnectionManager().setParams(params);
        }
    }

    public SimpleHttpClient(Map<String, String> taskArgs, JAXBContext jaxbContext, HttpConnectionManagerParams params) {
        this(taskArgs, jaxbContext, params, false);
    }

    private void initializeJAXBContext(JAXBContext jaxbContext) {
        if (jaxbContext != null) {
            this.jaxbContext = jaxbContext;
        } else {
            try {
                this.jaxbContext = JAXBContext.newInstance();
            } catch (JAXBException e) {
                logger.error("Exception while creating the default JAXB Context", e);
            }
        }
    }

    public static SimpleHttpClientBuilder builder(Map<String, String> taskArgs) {
        return new SimpleHttpClientBuilder(taskArgs);
    }

    protected void addProxyConfig() {
        HostConfiguration conf = httpClient.getHostConfiguration();
        String proxyUri = taskArgs.get(PROXY_URI);
        if (!Strings.isNullOrEmpty(proxyUri)) {
            try {
                URI uri = new URI(proxyUri);
                conf.setProxy(uri.getHost(), uri.getPort());
                setProxyCredentials(taskArgs);
            } catch (URISyntaxException e) {
                String msg = "The value of the property proxy-uri is not valid " + proxyUri;
                throw new IllegalArgumentException(msg, e);
            }
            logger.info("The ssl proxy is initialized with the url " + proxyUri);
        } else {
            String proxyHost = taskArgs.get(PROXY_HOST);
            if (!Strings.isNullOrEmpty(proxyHost)) {
                String proxyPort = taskArgs.get(PROXY_PORT);
                if (NumberUtils.isNumber(proxyPort)) {
                    conf.setProxy(proxyHost, Integer.parseInt(proxyPort));
                    setProxyCredentials(taskArgs);
                    logger.info("Proxy is initialized with the host = {} and port = {}", proxyHost, proxyPort);
                } else {
                    logger.warn("Cannot set the proxy, since the proxy port is not set. The proxy support is disabled");
                }
            }

        }
    }

    public WebTarget target() {
        return new WebTarget(this);
    }

    public WebTarget target(String url) {
        try {
            if (!Strings.isNullOrEmpty(url)) {
                URI uri = new URI(url);
                if (isSSLEnabled(uri)) {
                    String host = getHost(uri);
                    int port = getSSLPort(uri);
                    if (!Strings.isNullOrEmpty(host)) {
                        setEasySSLProtocol(host, port);
                    }
                }
                return new WebTarget(this).uri(url);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new WebTarget(this);
    }


    public Response get() {
        return new WebTarget(this).get();
    }

    public Response post(String data) {
        return new WebTarget(this).post(data);
    }


    protected void setProxyCredentials(Map<String, String> taskArgs) {
        String proxyUser = taskArgs.get(PROXY_USER);
        if (!Strings.isNullOrEmpty(proxyUser)) {
            String proxyPassword = getProxyPassword(taskArgs);
            if (!!Strings.isNullOrEmpty(proxyPassword)) {
                logger.warn("Proxy Password was not set, defaulting to empty");
                proxyPassword = "";
            }
            logger.debug("The credentials are set for the proxy with user = {}", proxyUser);
            AuthScope scope = new AuthScope(AuthScope.ANY);
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            httpClient.getState().setProxyCredentials(scope, creds);
        } else {
            logger.info("Proxy Credentials are not set, skipping");
        }
    }

    private String getProxyPassword(Map<String, String> taskArgs) {
        //Password can be an empty String also.
        if (taskArgs.containsKey(PROXY_PASSWORD)) {
            return taskArgs.get(PROXY_PASSWORD);
        } else if (taskArgs.containsKey(PROXY_PASSWORD_ENCRYPTED)) {
            String encrypted = taskArgs.get(PROXY_PASSWORD_ENCRYPTED);
            return encrypted;
            // return Encryptor.getInstance().decrypt(encrypted);
        }
        return null;
    }

    private void initializeSSL() {
        URI uri = getURI();
        if (isSSLEnabled(uri, taskArgs.get(TaskInputArgs.USE_SSL))) {
            String host = getHost(uri);
            int port = getSSLPort(uri);
            if (!Strings.isNullOrEmpty(host)) {
                setEasySSLProtocol(host, port);
                isSSLSupported = true;
            } else {
                logger.info("SSL support is disabled since the host is not set");
            }
        } else {
            logger.info("SSL support is is not enabled");
        }
    }

    private int getSSLPort(URI uri) {
        int port = getPort(uri);
        if (port < 0) {
            port = 443;
        }
        return port;
    }

    private void setEasySSLProtocol(String host, int port) {
        ProtocolSocketFactory factory = new EasySSLProtocolSocketFactory();
        Protocol protocol = new Protocol("https", factory, port);
        httpClient.getHostConfiguration().setHost(host, port, protocol);
    }

    private boolean isSSLEnabled(URI uri, String useSSL) {
        if (uri != null) {
            return isSSLEnabled(uri);
        } else {
            return !Strings.isNullOrEmpty(useSSL) && Boolean.valueOf(useSSL);
        }
    }

    private boolean isSSLEnabled(URI uri) {
        if (uri.getScheme() == null) {
            return false;
        }
        return uri.getScheme().equals("https");
    }

    private URI getURI() {
        String uriStr = taskArgs.get(TaskInputArgs.URI);

        if (!Strings.isNullOrEmpty(uriStr)) {
            try {
                return new URI(uriStr);
            } catch (URISyntaxException e) {
                logger.error("The URI is invalid " + uriStr, e);
            }
        }
        return null;
    }

    private int getPort(URI uri) {
        if (uri != null) {
            return uri.getPort();
        } else {
            String portStr = taskArgs.get(TaskInputArgs.PORT);
            if (NumberUtils.isNumber(portStr)) {
                return Integer.parseInt(portStr);
            } else {
                return -1;
            }

        }
    }

    private String getHost(URI uri) {
        if (uri != null) {
            return uri.getHost();
        } else {
            return taskArgs.get(TaskInputArgs.HOST);
        }
    }

    /**
     * Gets the underlying Apache Http Client 3.1.
     * This shouldn't be used directly unless some features need to be set that is not available OOTB.
     *
     * @return
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    protected Map<String, String> getTaskArgs() {
        return taskArgs;
    }

    protected JAXBContext getJaxbContext() {
        return jaxbContext;
    }

    protected AuthenticationConfig getAuthConfig() {
        return authConfig;
    }

    protected boolean isSSLSupported() {
        return isSSLSupported;
    }

    public void close() {
        if (httpConnectionManager instanceof MultiThreadedHttpConnectionManager) {
            MultiThreadedHttpConnectionManager
                    connectionManager = (MultiThreadedHttpConnectionManager) httpConnectionManager;
            connectionManager.shutdown();
        }
        if (httpConnectionManager instanceof SimpleHttpConnectionManager) {
            SimpleHttpConnectionManager
                    connectionManager = (SimpleHttpConnectionManager) httpConnectionManager;
            connectionManager.shutdown();

        }
    }
}
