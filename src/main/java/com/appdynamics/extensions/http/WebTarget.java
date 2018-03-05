/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

package com.appdynamics.extensions.http;

import com.google.common.base.Strings;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/7/14
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebTarget {
    public static final Logger logger = LoggerFactory.getLogger(WebTarget.class);

    private SimpleHttpClient simpleHttpClient;
    private UrlBuilder urlBuilder;
    private String accept;
    private String contentType;
    private Map<String, String> headers;


    public WebTarget(SimpleHttpClient simpleHttpClient) {
        this.simpleHttpClient = simpleHttpClient;
        this.urlBuilder = new UrlBuilder(simpleHttpClient.getTaskArgs());
    }

    public WebTarget ssl(boolean useSSL) {
        urlBuilder.ssl(useSSL);
        return this;
    }

    public WebTarget host(String host) {
        urlBuilder.host(host);
        return this;
    }

    public WebTarget port(String port) {
        urlBuilder.port(port);
        return this;
    }

    public WebTarget uri(String uri) {
        urlBuilder.uri(uri);
        return this;
    }

    public WebTarget path(String path) {
        urlBuilder.path(path);
        return this;
    }

    public WebTarget header(String name, String value) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put(name, value);
        return this;
    }

    public WebTarget query(String name, String value) {
        urlBuilder.query(name, value);
        return this;
    }

    public WebTarget accept(String type) {
        this.accept = type;
        return this;
    }

    public WebTarget type(String type) {
        this.contentType = type;
        return this;
    }

    public Response get() {
        String url = buildURL();
        GetMethod get = new GetMethod(url);
        addHeaders(get);
        try {
            HttpClient httpClient = simpleHttpClient.getHttpClient();
            int status = httpClient.executeMethod(get);
            validateStatus(url, status, get);
            return new Response(status, get, url, simpleHttpClient);
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching the data " + url, e);
        }
    }

    public Response post(String data) {
        String url = buildURL();
        logger.debug("Invoking the url={} with data={}", url, data);
        PostMethod post = new PostMethod(url);
        addHeaders(post);
        try {
            HttpClient httpClient = simpleHttpClient.getHttpClient();
            if (data != null) {
                post.setRequestEntity(new StringRequestEntity(data, null, null));
            }
            int status = httpClient.executeMethod(post);
            validateStatus(url, status, post);
            return new Response(status, post, url, simpleHttpClient);
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching the data " + url, e);
        }
    }

    private void validateStatus(String url, int status, HttpMethodBase method) {
        if (status != 200) {
            logger.error("The url {} responded with a status code of {} and message '{}'", url, status
                    , method.getStatusLine().getReasonPhrase());
            switch (status) {
                case 407:
                    throw new RuntimeException("The url '" + url + "' responded with a status code of 407. " +
                            "If you are using a proxy please make sure that you have set the correct proxy credentials");

                case 401:
                    throw new RuntimeException("It appears that the url '" + url + "' requires authentication. " +
                            "Please make sure that you have set the correct credentials");

            }
        }
    }

    /**
     * Well known issue, Apache HttpClient 3x doesnt work with absolute URI when host configuration is present
     *
     * @return
     */
    private String buildURL() {
        String url = urlBuilder.build();
        if (simpleHttpClient.isSSLSupported()) {
            try {
                URI uri = new URI(url);
                if (logger.isDebugEnabled()) {
                    logger.debug("The uri properties are absolute={} and scheme={}", uri.isAbsolute(), uri.getScheme());
                }
                if (uri.isAbsolute() && uri.getScheme().equals("https")) {
                    StringBuilder sb = new StringBuilder();
                    String path = uri.getPath();
                    if (!Strings.isNullOrEmpty(path)) {
                        sb.append(path);
                    }

                    String query = uri.getQuery();
                    if (!Strings.isNullOrEmpty(query)) {
                        sb.append("?").append(query);
                    }
                    String fragment = uri.getFragment();
                    if (!Strings.isNullOrEmpty(fragment)) {
                        sb.append("#").append(fragment);
                    }
                    url = sb.toString();
                    logger.debug("The relative uri is {}", url);
                }
            } catch (URISyntaxException e) {
                logger.error("The URI is not valid format '{}'", url, e);
            }
        }
        logger.debug("The final url is {}", url);
        return url;
    }


    private void addHeaders(HttpMethodBase get) {
        AuthenticationConfig authConfig = simpleHttpClient.getAuthConfig();
        if (authConfig != null) {
            get.addRequestHeader("Authorization", authConfig.getAuthHeader());
            logger.debug("Added the auth header ******");
        }

        if (accept != null) {
            get.addRequestHeader("Accept", accept);
        }
        if (contentType != null) {
            get.addRequestHeader("Content-Type", contentType);
        }
        if (headers != null) {
            for (String key : headers.keySet()) {
                get.addRequestHeader(key, headers.get(key));
            }
        }
    }

    public String getAbsoluteUrl() {
        return urlBuilder.build();
    }

    public String getUrl() {
        return buildURL();
    }

}
