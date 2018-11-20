package com.appdynamics.extensions.api;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

public class ControllerApiService {

    public static final org.slf4j.Logger logger = ExtensionsLoggerFactory.getLogger(ControllerApiService.class);
    ControllerInfo controllerInfo;

    public ControllerApiService(ControllerInfo controllerInfo) {
        this.controllerInfo = controllerInfo;
    }

    public CookiesCsrf getCookiesAndAuthToken(CloseableHttpClient httpClient) throws ApiException {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.host(controllerInfo.getControllerHost());
        urlBuilder.port(controllerInfo.getControllerPort());
        urlBuilder.ssl(controllerInfo.getControllerSslEnabled());

        HttpGet get = new HttpGet(urlBuilder.path("controller/auth?action=login").build());
        CloseableHttpResponse response = null;
        CookiesCsrf cookiesCsrf = new CookiesCsrf();
        StatusLine statusLine;
        try {
            response = httpClient.execute(get);
            statusLine = response.getStatusLine();
            if (statusLine != null && statusLine.getStatusCode() == 200) {
                Header[] headers = response.getAllHeaders();
                StringBuilder cookies = new StringBuilder();
                String csrf = null;
                for (Header header : headers) {
                    if (header.getName().equalsIgnoreCase("set-cookie")) {
                        String value = header.getValue();
                        cookies.append(value).append(";");
                        if (value.toLowerCase().contains("x-csrf-token")) {
                            csrf = value.split("=")[1];
                            if (csrf.contains(";")) {
                                csrf = csrf.split(";")[0].trim();
                            }
                        }
                    }
                }
                logger.debug("Setting Cookies to : {}", cookies.toString());
                cookiesCsrf.setCookies(cookies.toString());
                if (!Strings.isNullOrEmpty(csrf)) {
                    cookiesCsrf.setCsrf(csrf);
                }
                logger.debug("The controller login is successful, the cookie is [{}] and csrf is {}", cookies, csrf);
            } else if (statusLine != null) {
                logger.error("Custom Dashboard Upload Failed. The login to the controller is unsuccessful. The response code is {}"
                        , statusLine.getStatusCode());
                logger.error("The response headers are {} and content is {}", Arrays.toString(response.getAllHeaders()), response.getEntity());
            }
            return cookiesCsrf;

        } catch (IOException e) {
            throw new ApiException("Error in controller login", e);
        } finally {
            closeHttpResponse(response);
        }
    }

    public JsonNode getAllDashboards(CloseableHttpClient httpClient, CookiesCsrf cookiesCsrf) throws ApiException {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.host(controllerInfo.getControllerHost());
        urlBuilder.port(controllerInfo.getControllerPort());
        urlBuilder.ssl(controllerInfo.getControllerSslEnabled());

        HttpGet get = new HttpGet(urlBuilder.path("controller/restui/dashboards/getAllDashboardsByType/false").build());
        if (!Strings.isNullOrEmpty(cookiesCsrf.getCookies())) {
            get.setHeader("Cookie", cookiesCsrf.getCookies());
        }
        if (!Strings.isNullOrEmpty(cookiesCsrf.getCsrf())) {
            get.setHeader("X-CSRF-TOKEN", cookiesCsrf.getCsrf());
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(get);
            JsonNode arrayNode = null;
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null && statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                arrayNode = new ObjectMapper().readValue(EntityUtils.toString(entity), JsonNode.class);
            } else if (statusLine != null) {
                logger.error("The controller API returned an invalid response {}, so cannot get a list of all dashboards."
                        , statusLine.getStatusCode());
            }
            return arrayNode;
        } catch (IOException e) {
            throw new ApiException("Error in getting all dashboards", e);
        } finally {
            closeHttpResponse(response);
        }
    }

    private void closeHttpResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ex) {
                logger.error("Error encountered while closing the HTTP response", ex);
            }
        }
    }


    /*
      #TODO This uses basic http to do Multi-part form upload. Need to change this by using the apache client once the dependency with MA is resolved.
     */

    public void uploadDashboard(Map<String, ?> httpProperties, CookiesCsrf cookiesCsrf, String dashboardName, String fileExtension, String fileContent, String fileContentType) throws ApiException {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.host(controllerInfo.getControllerHost());
        urlBuilder.port(controllerInfo.getControllerPort());
        urlBuilder.ssl(controllerInfo.getControllerSslEnabled());

        String filename = dashboardName + "." + fileExtension;
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";
        String urlStr = urlBuilder.path("controller/CustomDashboardImportExportServlet").build();
        logger.info("Uploading the custom Dashboard {} to {}", filename, urlStr);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            if (httpProperties.containsKey("proxy")) {
                Map<String, ?> proxyMap = (Map<String, ?>) httpProperties.get("proxy");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((String) proxyMap.get(TaskInputArgs.HOST)
                        , Integer.parseInt((String) proxyMap.get(TaskInputArgs.PORT))));
                connection = (HttpURLConnection) url.openConnection(proxy);
                logger.debug("Created an HttpConnection for Fileupload with a proxy {}", proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                httpsURLConnection.setSSLSocketFactory(createSSLSocketFactory());
                httpsURLConnection.setHostnameVerifier(new CustomHostnameVerifier());
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
                throw new ApiException("Error while uploading the dashboard", e);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException("Error in uploading dashboard", e);
        }

    }


    private static SSLSocketFactory createSSLSocketFactory() throws ApiException {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
            }, new SecureRandom());
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException("Unsupported algorithm", e);
        } catch (KeyManagementException e) {
            throw new ApiException("Key Management exception", e);
        }
    }

    private static class CustomHostnameVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

}
