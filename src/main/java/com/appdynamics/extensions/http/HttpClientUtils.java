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

package com.appdynamics.extensions.http;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by abey.tom on 3/15/16.
 * Utility methods to invoke the http end point using the given http client.
 * All these methods are silent methods, it will not throw errors. It will return null response on error.
 * It will take care of logging the error and will log the response content if debug logging is enabled.
 */
public class HttpClientUtils {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(HttpClientUtils.class);

    public static <T> T getResponseAsJson(CloseableHttpClient httpClient, final String url, final Class<T> clazz) {
        return getResponseAsJson(httpClient, url, clazz, Collections.<String, String>emptyMap());
    }

    public static <T> T getResponseAsJson(CloseableHttpClient httpClient, final String url, final Class<T> clazz, Map<String, String> headers) {
        ResponseConverter<T> responseConverter = new ResponseConverter<T>() {
            public T convert(HttpEntity entity) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    //If the debug is enabled, then print the response.
                    // This way is more efficient while running on non debug
                    if (logger.isDebugEnabled()) {
                        String json = EntityUtils.toString(entity);
                        logger.debug("The response of url [{}] is {}", url, json);
                        return mapper.readValue(json, clazz);
                    } else {
                        return mapper.readValue(entity.getContent(), clazz);
                    }
                } catch (IOException e) {
                    logger.error("Error while converting the response of [" + url + "] to JSON", e);
                    return null;
                }
            }
        };
        return getResponse(httpClient, url, responseConverter, headers);
    }

    public static String getResponseAsStr(CloseableHttpClient httpClient, final String url) {
        return getResponse(httpClient, url, new ResponseConverter<String>() {
            public String convert(HttpEntity entity) {
                try {
                    String response = EntityUtils.toString(entity);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The response of the url [{}]  is [{}]", url, response);
                    }
                    return response;
                } catch (IOException e) {
                    logger.error("Error while converting response of url [" + url + "] to string " + entity, e);
                    return null;
                }
            }
        });
    }

    public static List<String> getResponseAsLines(CloseableHttpClient httpClient, final String url) {
        return getResponse(httpClient, url, new ResponseConverter<List<String>>() {
            public List<String> convert(HttpEntity entity) {
                try {
                    List<String> lines = IOUtils.readLines(entity.getContent());
                    if (logger.isDebugEnabled()) {
                        logger.debug("The response of the url [{}]  is [{}]", url, lines);
                    }
                    return lines;
                } catch (IOException e) {
                    logger.error("Error while converting response of url [" + url + "] to lines " + entity, e);
                    return null;
                }
            }
        });
    }

    public static <T> T getResponse(CloseableHttpClient httpClient, String url, ResponseConverter<T> converter) {
        return getResponse(httpClient, url, converter, Collections.<String, String>emptyMap());
    }

    public static <T> T getResponse(CloseableHttpClient httpClient, String url, ResponseConverter<T> converter, Map<String, String> headers) {
        logger.debug("Invoking the URL [{}]", url);
        HttpGet get = new HttpGet(url);
        if (headers != null && !headers.isEmpty()) {
            for (String name : headers.keySet()) {
                get.addHeader(name, headers.get(name));
            }
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(get);
            StatusLine statusLine;
            if (response != null
                    && ((statusLine = response.getStatusLine()) != null)
                    && statusLine.getStatusCode() == 200) {
                logger.trace("The response headers are {}", response.getAllHeaders());
                HttpEntity entity = response.getEntity();
                if (entity != null && entity.getContent() != null) {
                    return converter.convert(entity);
                } else {
                    logger.error("The entity content is null. Entity = {} and Content = {}", entity, entity.getContent());
                }
            } else {
                printError(response, url);
            }
        } catch (Exception e) {
            printError(response, url);
            logger.error("Exception while executing the request [" + url + "]", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }


    public static void printError(CloseableHttpResponse response, String url) {
        if (response != null) {
            logger.error("The status line for the url [{}] is [{}] and the headers are [{}]"
                    , url, response.getStatusLine(), response.getAllHeaders());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    logger.error("The contents are {}", EntityUtils.toString(response.getEntity()));
                } catch (Exception e) {
                    logger.error("", e);
                }
            } else {
                logger.error("The response content is null");
            }
        } else {
            logger.error("The response is null for the URL {}", url);
        }
    }

    /**
     * This class will be invoked to convert the response to a suitable format.
     *
     * @param <T>
     */
    public interface ResponseConverter<T> {
        T convert(HttpEntity entity);
    }

    public static void closeHttpResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ex) {
                logger.error("Error encountered while closing the HTTP response", ex);
            }
        }
    }
}
