package com.appdynamics.extensions;

import java.util.List;

interface ConcealedMonitorExecutorService {

    void shutdown();

    List<Runnable> shutdownNow();

    boolean isShutdown();

}
