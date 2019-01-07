package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.CookiesCsrf;
import com.appdynamics.extensions.crypto.Decryptor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static com.appdynamics.extensions.Constants.URI;

/**
 * Created by venkata.konala on 1/1/19.
 */
public class CustomDashboardAPIService extends APIService{

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardAPIService.class);

    CustomDashboardAPIService(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        super(controllerInfo, controllerClient);
    }

    public JsonNode getAllDashboards() {
        if(controllerClient != null) {
            JsonNode allDashboardsNode = null;
            String alldashboards;
            try {
                alldashboards = controllerClient.sendGetRequest("controller/restui/dashboards/getAllDashboardsByType/false");
                allDashboardsNode = new ObjectMapper().readTree(alldashboards);
            } catch (ControllerHttpRequestException e) {
                logger.error("Invalid response from controller while fetching information about all dashbards", e);
            } catch (IOException e) {
                logger.error("Error while getting all dashboards information", e);
            }
            return allDashboardsNode;
        }
        logger.debug("The controllerClient is not initialized");
        return null;
    }

    // #TODO Check if cookiesCsrf from a different HttpClient can be used.
    public void uploadDashboard(Map<String, ?> propMap, String dashboardName, String fileExtension, String fileContent, String fileContentType) throws ControllerHttpRequestException {
        Map<String, ?> connectionMap = (Map<String, ?>)propMap.get("connection");
        Map<String, ?> proxyMap = (Map<String, ?>)propMap.get("proxy");

        CookiesCsrf cookiesCsrf = controllerClient.getCookiesCsrf();
        String filename = dashboardName + "." + fileExtension;
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";
        String urlStr = controllerClient.getBaseURL() + "controller/CustomDashboardImportExportServlet";
        logger.info("Uploading the custom Dashboard {} to {}", filename, urlStr);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            if (proxyMap != null && !proxyMap.isEmpty() && !Strings.isNullOrEmpty((String)proxyMap.get(URI))) {
                URL proxyURL = new URL((String)proxyMap.get(URI));
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyURL.getHost()
                        , proxyURL.getPort()));
                connection = (HttpURLConnection) url.openConnection(proxy);
                logger.debug("Created an HttpConnection for Fileupload with a proxy {}", proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                httpsURLConnection.setSSLSocketFactory(createSSLSocketFactory(propMap));
                httpsURLConnection.setHostnameVerifier(createHostNameVerifier(connectionMap));
            }
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("Cookie", cookiesCsrf.getCookies());

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.writeBytes(twoHyphens + boundary + lineEnd);
            request.writeBytes("Content-Disposition: form-data; name=\"" + dashboardName + "\";filename=\"" + filename + "\"" + lineEnd);
            request.writeBytes("Content-Type: " + fileContentType + lineEnd);
            request.writeBytes(lineEnd);
            request.write(fileContent.getBytes());
            request.writeBytes(lineEnd + lineEnd);
            request.writeBytes(twoHyphens + boundary + lineEnd);

            if (cookiesCsrf.getCsrf() != null) {
                request.writeBytes("Content-Disposition: form-data; name=\"X-CSRF-TOKEN\"" + lineEnd);
                request.writeBytes(lineEnd);
                request.writeBytes(cookiesCsrf.getCsrf());
                request.writeBytes(lineEnd);
                request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            } else {
                logger.warn("The CSRF is null, trying the dashboard upload without the CSRF token");
            }

            request.flush();
            request.close();
            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
                int status = connection.getResponseCode();
                if (status == 200) {
                    logger.info("Successfully Imported the dashboard {}", filename);
                } else {
                    logger.error("The server responded with a status of {}", status);
                    String content = null;
                    if (inputStream != null) {
                        content = IOUtils.toString(inputStream);
                    }
                    logger.error("The response headers are {} and content is [{}]",
                            connection.getHeaderFields(), content);
                }

            } catch (IOException e) {
                logger.error("Error while uploading the dashboard " + urlStr, e);
                InputStream errorStream = connection.getErrorStream();
                String content = null;
                if (errorStream != null) {
                    content = IOUtils.toString(errorStream);
                }
                logger.error("The error response headers are {} and content is [{}]",
                        connection.getHeaderFields(), content);
                throw new ControllerHttpRequestException("Error while uploading the dashboard", e);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            throw new ControllerHttpRequestException("Error in uploading dashboard", e);
        }

    }

    /*#TODO The SSLContext creation needs to be a common utility available fot all clients.*/
    private static SSLSocketFactory createSSLSocketFactory(Map<String, ?> propMap) throws Exception {
        try {
            Map<String, ?> connectionMap = (Map<String, ?>)propMap.get("connection");
            //#TODO Add protocols from connections.
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            KeyStore keyStore = buildKeyStore(connectionMap);
            char[] keyStorePassword = getKeyStorePassword(connectionMap, (String)propMap.get("encryptionKey"));
            KeyStore trustStore = buildTrustStore(connectionMap);

            KeyManager[] keyManagers = null;

            //#TODO Check for an empty keystore.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            context.init( keyManagers, trustManagers, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception e) {
            throw e;
        }
    }

    //#TODO Need to fall back on ma-cacerts as well
    private static KeyStore buildKeyStore(Map<String, ?> connectionMap) {
        KeyStore keyStore;
        if(connectionMap != null && connectionMap.get("sslKeyStorePath") != null && connectionMap.get("sslKeyStorePassword") != null) {
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                String keyStorePath = (String)connectionMap.get("sslKeyStorePath");
                String keyStorePassword = (String)connectionMap.get("sslKeyStorePassword");
                InputStream inputStream = new FileInputStream(keyStorePath);
                keyStore.load(inputStream, keyStorePassword.toCharArray());
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Exception while creating a custom keystore. Will fallback on the jre default keystore if available", e);
            }
        }
        return null;
    }

    private static char[] getKeyStorePassword(Map<String, ?> connectionMap, String encryptionKey) {
        if (connectionMap != null) {
            String password = (String)connectionMap.get("sslKeyStorePassword");
            String encryptedPassword = (String)connectionMap.get("sslKeyStoreEncryptedPassword");
            if (!Strings.isNullOrEmpty(password)) {
                return password.toCharArray();
            } else if (!Strings.isNullOrEmpty(encryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)) {
                return new Decryptor(encryptionKey).decrypt(encryptedPassword).toCharArray();
            }
        }
        logger.warn("Returning null password for sslKeyStore");
        return null;
    }

    //#TODO Need to fall back on ma-cacerts as well
    // #TODO Add extensions-cacerts
    private static KeyStore buildTrustStore(Map<String, ?> connectionMap) {
        KeyStore trustStore;
        if(connectionMap != null && connectionMap.get("sslTrustStorePath") != null && connectionMap.get("sslTrustStorePassword") != null) {
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                String trustStorePath = (String)connectionMap.get("sslTrustStorePath");
                String trustStorePassword = (String)connectionMap.get("sslTrustStorePassword");
                InputStream inputStream = new FileInputStream(trustStorePath);
                trustStore.load(inputStream, trustStorePassword.toCharArray());
                return trustStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Exception while creating a custom truststore. Will fallback on the jre default truststore if available", e);
            }
        }
        return null;
    }

    public static HostnameVerifier createHostNameVerifier(Map<String, ?> connectionMap) {
        if(connectionMap != null && connectionMap.get("sslVerifyHostname") != null && (Boolean)connectionMap.get("sslVerifyHostname")) {
            return new BrowserCompatHostnameVerifier();
        }
        return new AllHostnameVerifier();
    }

    public static class AllHostnameVerifier implements X509HostnameVerifier {
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
