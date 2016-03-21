package com.appdynamics.extensions.http;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by abey.tom on 3/15/16.
 */
public class HttpClientUtils {

    public static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    public static <T> T getResponseAsJson(CloseableHttpClient httpClient, final String url, final Class<T> clazz) {
        Callback<T> callback = new Callback<T>() {
            public T getResponse(HttpEntity entity) {
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
        return getResponse(httpClient, url, callback);
    }

    private static <T> T getResponse(CloseableHttpClient httpClient, String url, Callback<T> callback) {
        logger.debug("Invoking the URL [{}]", url);
        HttpGet get = new HttpGet(url);
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
                    return callback.getResponse(entity);
                } else {
                    logger.error("The entity content is null. Entity = {} and Content = {}", entity, entity.getContent());
                }
            } else {
                printError(response, url);
            }
        } catch (Exception e) {
            printError(response, url);
            logger.error("Exception while executing the request [" + url + "]", e);
        }
        return null;
    }


    private static void printError(CloseableHttpResponse response, String url) {
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

    public interface Callback<T> {
        T getResponse(HttpEntity entity);
    }
}
