package com.appdynamics.extensions.conf.configurationModules;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class HttpClientModule {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientModule.class);
    private CloseableHttpClient httpClient;

    public CloseableHttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        else {
            throw new RuntimeException("Cannot Initialize HttpClient.The [servers] section is not set in the config.yml");
        }
    }

    public void initHttpClient(Map<String, ?> config) {
        initShutdown(httpClient, 2000 * 60);
        List servers = (List) config.get("servers");
        if (servers != null && !servers.isEmpty()) {
            httpClient = Http4ClientBuilder.getBuilder(config).build();
        }
        else {
            logger.info("The httpClient is not initialized since the [servers] are not present in config.yml");
        }
    }

    private void initShutdown(final CloseableHttpClient oldHttpClient, final long wait) {
        if (oldHttpClient != null) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(wait);
                        logger.debug("Shutting down the old http client {}", httpClient);
                        oldHttpClient.close();
                    } catch (Exception e) {
                        logger.error("Exception while shutting down the http client" + oldHttpClient, e);
                    }
                }
            }, "HttpClient-Shutdown-Task").start();
        }
    }
}

