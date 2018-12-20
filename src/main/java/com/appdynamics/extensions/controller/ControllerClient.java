package com.appdynamics.extensions.controller;

import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by venkata.konala on 12/19/18.
 */
// #TODO Check about the logger required for healthchecks
public class ControllerClient {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerClient.class);
    private CloseableHttpClient controllerHttpClient;
    private String controllerBaseURL;
    // #TODO This needs to be synchronized
    private CookiesCsrf cookiesCsrf;

    private ControllerClient() {
    }

    private static ControllerClient controllerClient = new ControllerClient();

    static ControllerClient getControllerClient(){
        return controllerClient;
    }

    void setControllerHttpClient(CloseableHttpClient controllerHttpClient) {
        this.controllerHttpClient = controllerHttpClient;
    }

    public CloseableHttpClient getControllerHttpClient() {
        return controllerHttpClient;
    }

    void setControllerBaseURL(String controllerBaseURL) {
        this.controllerBaseURL = controllerBaseURL;
    }

    public String getControllerBaseURL() {
        return controllerBaseURL;
    }

    void setCookiesCsrf(CookiesCsrf cookiesCsrf) {
        this.cookiesCsrf = cookiesCsrf;
    }

    public CookiesCsrf getCookiesCsrf() throws ControllerHttpRequestException{
        if (cookiesCsrf == null) {
            cookiesCsrf = getCookiesAndAuthToken();
        }
        return cookiesCsrf;
    }

    public String sendGetRequest(String url) throws ControllerHttpRequestException {
        if(cookiesCsrf == null) {
            cookiesCsrf = getCookiesAndAuthToken();
        }
        HttpGet get = new HttpGet(controllerBaseURL + url);
        if (!Strings.isNullOrEmpty(cookiesCsrf.getCsrf())) {
            get.setHeader("X-CSRF-TOKEN", cookiesCsrf.getCsrf());
        }
        get.setHeader("Cookie", cookiesCsrf.getCookies());
        if (!Strings.isNullOrEmpty(cookiesCsrf.getCsrf())) {
            get.setHeader("X-CSRF-TOKEN", cookiesCsrf.getCsrf());
        }
        CloseableHttpResponse response = null;
        try {
            response = controllerHttpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            String responseString = null;
            if (statusLine != null && statusLine.getStatusCode() == 200) {
                responseString = EntityUtils.toString(response.getEntity());
                logger.debug("Response for url [{}] is [{}]", url, responseString);
            } else if (statusLine != null) {
                logger.error("The controller API returned an invalid response {}, so cannot get a list of all dashboards."
                        , statusLine.getStatusCode());
            }
            return responseString;
        } catch (Exception e) {
            throw new ControllerHttpRequestException("Error while sending a get request to the controller", e);
        } finally {
            HttpClientUtils.closeHttpResponse(response);
        }
    }

    // #TODO Need to check if the tokens expires in a set time.
    private CookiesCsrf getCookiesAndAuthToken()throws ControllerHttpRequestException{
        HttpGet get = new HttpGet(controllerBaseURL + "controller/auth?action=login");
        CloseableHttpResponse response = null;
        CookiesCsrf cookiesCsrf = new CookiesCsrf();
        StatusLine statusLine;
        try {
            response = controllerHttpClient.execute(get);
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
            throw new ControllerHttpRequestException("Error in controller login", e);
        } finally {
            HttpClientUtils.closeHttpResponse(response);
        }
    }
}
