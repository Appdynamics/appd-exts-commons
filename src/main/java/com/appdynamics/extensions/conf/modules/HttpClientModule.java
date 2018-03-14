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

package com.appdynamics.extensions.conf.modules;

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

