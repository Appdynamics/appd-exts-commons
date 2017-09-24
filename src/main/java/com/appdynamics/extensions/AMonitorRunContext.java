package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AMonitorRunContext holds the state across a complete AMonitorTaskRunner's run.
 *
 */

public class AMonitorRunContext {

    private ABaseMonitor aBaseMonitor;
    private AtomicInteger taskCounter;
    private MetricWriteHelper metricWriteHelper;


    public AMonitorRunContext(ABaseMonitor aBaseMonitor) {
        this.aBaseMonitor = aBaseMonitor;
        taskCounter = new AtomicInteger(this.aBaseMonitor.getTaskCount());
        metricWriteHelper = MetricWriteHelperFactory.create(aBaseMonitor);
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
                        onRunComplete();
                    }
                }
            }
        });
    }

    private void onRunComplete(){
        metricWriteHelper.onComplete();
        aBaseMonitor.onComplete();
    }

    public MetricWriteHelper getMetricWriteHelper() {
        return this.metricWriteHelper;
    }

    public MonitorConfiguration getMonitorConfiguration(){
        return this.aBaseMonitor.configuration;
    }
}
