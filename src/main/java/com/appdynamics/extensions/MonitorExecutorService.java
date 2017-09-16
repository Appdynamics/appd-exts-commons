package com.appdynamics.extensions;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface MonitorExecutorService extends ConcealedMonitorExecutorService {

    Future<?> submit(String name,Runnable task);

    void execute(String name,Runnable command);

    <T> Future<T> submit(String name,Callable<T> task);

}

