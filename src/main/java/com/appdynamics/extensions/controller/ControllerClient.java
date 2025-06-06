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

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerClient {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerClient.class);
    private CloseableHttpClient httpClient;
    private String baseURL;
    private CookiesCsrf cookiesCsrf;

    ControllerClient() {
    }

    void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    void setCookiesCsrf(CookiesCsrf cookiesCsrf) {
        this.cookiesCsrf = cookiesCsrf;
    }

    // #TODO Check the CSRF token expiry
    // #TODO Make it private once dashboard uploader is updated to Apache HttpClient 4.4+
    public synchronized CookiesCsrf getCookiesCsrf() throws ControllerHttpRequestException{
        if (cookiesCsrf == null) {
            cookiesCsrf = getCookiesAndAuthToken();
            logger.debug("The cookiesCsrf of the ControllerClient has been set.");
        }
        return cookiesCsrf;
    }

    public String sendGetRequest(String url) throws ControllerHttpRequestException {
        getCookiesCsrf();
        HttpGet get = new HttpGet(baseURL + url);
        if (!Strings.isNullOrEmpty(cookiesCsrf.getCsrf())) {
            get.setHeader("Cookie", cookiesCsrf.getCookies());
            get.setHeader("X-CSRF-TOKEN", cookiesCsrf.getCsrf());
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            String responseString = null;
            if (statusLine != null && statusLine.getStatusCode() == 200) {
                responseString = EntityUtils.toString(response.getEntity());
                logger.debug("Response for url [{}] is [{}]", url, responseString);
            } else if (statusLine != null) {
                logger.error("The controller API returned an invalid response {}"
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
        HttpGet get = new HttpGet(baseURL + "controller/auth?action=login");
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
                logger.error("The login to the controller is unsuccessful. The response code is {}"
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
