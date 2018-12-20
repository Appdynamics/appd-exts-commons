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

package com.appdynamics.extensions;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.*;

/**
 * Thread Pool Executor to be used for extensions.
 */
public class MonitorThreadPoolExecutor implements MonitorExecutorService {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MonitorThreadPoolExecutor.class);
    private static final long TASK_TIME_THRESHOLD_IN_MS = 60 * 1000l;

    private ThreadPoolExecutor executor;

    public MonitorThreadPoolExecutor(ThreadPoolExecutor executor) {
        AssertUtils.assertNotNull(executor, "Threadpool executor cannot be null.");
        this.executor = executor;
    }

    @Override
    public Future<?> submit(String name, Runnable task) {
        return executor.submit(wrapWithRunnable(name, task));
    }

    @Override
    public void execute(String name, Runnable task) {
        executor.execute(wrapWithRunnable(name, task));
    }

    @Override
    public <T> Future<T> submit(String name, Callable<T> task) {
        return executor.submit(wrapWithCallable(name, task));
    }

    @Override
    public void scheduleAtFixedRate(String name, Runnable task, int initialDelaySeconds, int taskDelaySeconds, TimeUnit seconds) {
        if (executor instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor) executor;
            scheduledExecutor.scheduleAtFixedRate(wrapWithRunnable(name, task), initialDelaySeconds, taskDelaySeconds, seconds);
        } else {
            throw new RuntimeException("The ThreadPoolExecutor being used does not support scheduleAtFixedRate() method");
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    private <T> Callable wrapWithCallable(final String name, final Callable<T> task) {
        return new Callable() {
            @Override
            public Object call() throws Exception {
                long startTime = System.currentTimeMillis();
                T t = task.call();
                long diffTime = System.currentTimeMillis() - startTime;
                if (diffTime > TASK_TIME_THRESHOLD_IN_MS) {  //time limit for each server task to finish
                    logger.warn("{} Task took {} ms to complete", name, diffTime);
                }
                return t;
            }
        };
    }

    private Runnable wrapWithRunnable(final String name, final Runnable task) {
        return new TaskRunnable(name, task);
    }

    private class TaskRunnable implements Runnable {

        private String name;
        private Runnable task;

        TaskRunnable(String name, Runnable task) {
            this.name = name;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                task.run();
                long diffTime = System.currentTimeMillis() - startTime;
                if (diffTime > TASK_TIME_THRESHOLD_IN_MS) {  //time limit for each server task to finish
                    logger.warn("{} Task took {} ms to complete", name, diffTime);
                }
            } catch (Exception e) {
                logger.error("Error while running the Task {}", name, e);
            }
        }

    }
}
