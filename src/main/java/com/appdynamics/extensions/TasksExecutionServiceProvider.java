package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * TaskExecutionServiceProvider is responsible for submitting all the tasks of
 * a job to the ExecutorService and also making sure that all the job tasks are
 * completed before executing the onComplete() method of MetricWriteHelper.
*/
public class TasksExecutionServiceProvider {

    private ABaseMonitor aBaseMonitor;
    private AtomicInteger taskCounter;
    private MetricWriteHelper metricWriteHelper;


    public TasksExecutionServiceProvider(ABaseMonitor aBaseMonitor, MetricWriteHelper metricWriteHelper) {
        this.aBaseMonitor = aBaseMonitor;
        this.metricWriteHelper = metricWriteHelper;
        taskCounter = new AtomicInteger(this.aBaseMonitor.getTaskCount());
    }


    public void submit(final String name, final AMonitorTaskRunnable aServerTask){
        aBaseMonitor.getConfiguration().getExecutorService().submit(name,new Runnable() {
            @Override
            public void run() {
                try{
                    aServerTask.run();
                    aServerTask.onTaskComplete();
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
