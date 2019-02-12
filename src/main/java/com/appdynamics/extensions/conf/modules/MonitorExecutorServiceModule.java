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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.executorservice.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.YmlUtils;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class MonitorExecutorServiceModule {

    static final Logger logger = ExtensionsLoggerFactory.getLogger(MonitorExecutorServiceModule.class);
    private MonitorExecutorService executorService;
    private int executorServiceSize;

    public MonitorExecutorService getExecutorService() {
        if (executorService != null) {
            return executorService;
        } else {
            throw new RuntimeException("The executor service is not initialized. Please make sure that the params are set in config.yml");
        }
    }

    public void initExecutorService(Map<String, ?> config, String monitorName) {
        Integer numberOfThreads = YmlUtils.getInteger(config.get("numberOfThreads"));
        Integer queueCapacityGrowthFactor = YmlUtils.getInt(config.get("queueCapacityGrowthFactor"), 10);
        if (numberOfThreads != null) {
            if (executorService == null) {
                executorService = createThreadPool(numberOfThreads, numberOfThreads * queueCapacityGrowthFactor, monitorName);
            } else if (numberOfThreads != executorServiceSize) {
                logger.info("The ThreadPool size has been updated from {} -> {}", executorServiceSize, numberOfThreads);
                executorService.shutdown();
                executorService = createThreadPool(numberOfThreads, numberOfThreads * queueCapacityGrowthFactor, monitorName);
            }
            executorServiceSize = numberOfThreads;
        } else {
            logger.info("Not initializing the thread pools since the [numberOfThreads] is not set");
            executorServiceSize = 0;
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    private MonitorExecutorService createThreadPool(Integer numberOfThreads, Integer queueCapacity, String monitorName) {
        if (numberOfThreads != null && numberOfThreads > 0) {
            logger.info("Initializing the ThreadPool with size {}", numberOfThreads);
            ThreadPoolExecutor executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(queueCapacity),
                    new ThreadFactory() {
                        private int count;

                        public Thread newThread(Runnable r) {
                            Thread thread = new Thread(r, "Monitor-Task-Thread" + (++count));
                            thread.setContextClassLoader(AManagedMonitor.class.getClassLoader());
                            return thread;
                        }
                    },
                    new ThreadPoolExecutor.DiscardPolicy() {
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                            logger.error("Queue Capacity reached!! Rejecting runnable tasks..");
                        }
                    }
            );
            return new MonitorThreadPoolExecutor(executor);

        } else {
            return null;
        }
    }
}
