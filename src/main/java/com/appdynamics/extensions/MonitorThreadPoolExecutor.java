package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Thread Pool Executor to be used for extensions.
 */
public class MonitorThreadPoolExecutor implements MonitorExecutorService {

    public static final Logger logger = LoggerFactory.getLogger(MonitorConfiguration.class);
    private static final long TASK_TIME_THRESHOLD_IN_MS = 60 * 1000l;

    private ThreadPoolExecutor executor;

    public MonitorThreadPoolExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Future<?> submit(String name, Runnable task) {
        return executor.submit(wrapWithRunnable(name,task));
    }

    @Override
    public void execute(String name, Runnable command) {
        executor.execute(wrapWithRunnable(name,command));
    }

    @Override
    public <T> Future<T> submit(String name,Callable<T> task) {
        return executor.submit(wrapWithCallable(name,task));
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
                if(diffTime > TASK_TIME_THRESHOLD_IN_MS){  //time limit for each server task to finish
                    logger.warn("{} Task took {} ms to complete",name,diffTime);
                }
                return t;
            }
        };
    }

    private Runnable wrapWithRunnable(final String name,final Runnable task) {
        return new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                task.run();
                long diffTime = System.currentTimeMillis() - startTime;
                if(diffTime > TASK_TIME_THRESHOLD_IN_MS){  //time limit for each server task to finish
                    logger.warn("{} Task took {} ms to complete",name,diffTime);
                }
            }
        };
    }
}
