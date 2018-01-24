package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.apache.commons.httpclient.util.ExceptionUtil;
import org.apache.http.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * TaskExecutionServiceProvider is responsible for submitting all the tasks of
 * a job to the ExecutorService and also making sure that all the job tasks are
 * completed before executing the onComplete() method of MetricWriteHelper.
*/
public class TasksExecutionServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(TasksExecutionServiceProvider.class);
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
                catch (Throwable e){
                    logger.error("Unforeseen error or exception happened", e);
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
