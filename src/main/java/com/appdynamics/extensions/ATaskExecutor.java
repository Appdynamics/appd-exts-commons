package com.appdynamics.extensions;

import java.util.concurrent.atomic.AtomicInteger;

public class ATaskExecutor {

    private AtomicInteger taskCounter;
    private ABaseMonitor aBaseMonitor;

    public ATaskExecutor(ABaseMonitor aBaseMonitor) {
        taskCounter = new AtomicInteger(aBaseMonitor.getTaskCount());
        this.aBaseMonitor = aBaseMonitor;
    }

    public void submit(final String name, final Runnable aServerTask){
        aBaseMonitor.configuration.getExecutorService().submit(name,new Runnable() {
            @Override
            public void run() {
                try{
                    aServerTask.run();
                }
                finally {
                    if(taskCounter.decrementAndGet() <= 0){
                        aBaseMonitor.onComplete();
                    }
                }
            }
        });
    }
}
