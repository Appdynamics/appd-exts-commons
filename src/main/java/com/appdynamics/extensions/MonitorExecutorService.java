package com.appdynamics.extensions;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface MonitorExecutorService extends ConcealedMonitorExecutorService {

    Future<?> submit(String name,Runnable task);

    void execute(String name,Runnable task);

    <T> Future<T> submit(String name,Callable<T> task);

    void scheduleAtFixedRate(String name,Runnable task, int initialDelaySeconds, int taskDelaySeconds, TimeUnit seconds);
}

