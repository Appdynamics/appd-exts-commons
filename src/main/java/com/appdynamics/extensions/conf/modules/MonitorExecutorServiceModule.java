package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.util.YmlUtils;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class MonitorExecutorServiceModule {

    static final Logger logger = LoggerFactory.getLogger(MonitorExecutorServiceModule.class);
    private MonitorExecutorService executorService;
    private int executorServiceSize;

    public MonitorExecutorService getExecutorService() {
        if (executorService != null) {
            return executorService;
        } else {
            throw new RuntimeException("The executor service is not initialized. Please make sure that the params are set in config.yml");
        }
    }

    public void initExecutorService(Map<String, ?> config) {
        Integer numberOfThreads = YmlUtils.getInteger(config.get("numberOfThreads"));
        Integer queueCapacityGrowthFactor = YmlUtils.getInt(config.get("queueCapacityGrowthFactor"),10);
        if (numberOfThreads != null) {
            if (executorService == null) {
                executorService = createThreadPool(numberOfThreads,numberOfThreads * queueCapacityGrowthFactor);
            } else if (numberOfThreads != executorServiceSize) {
                logger.info("The ThreadPool size has been updated from {} -> {}", executorServiceSize, numberOfThreads);
                executorService.shutdown();
                executorService = createThreadPool(numberOfThreads,numberOfThreads * queueCapacityGrowthFactor);
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

    private MonitorExecutorService createThreadPool(Integer numberOfThreads, Integer queueCapacity) {
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
                    new ThreadPoolExecutor.DiscardPolicy(){
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
