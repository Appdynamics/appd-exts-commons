package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.AMonitorTaskRunner;
import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.util.YmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class TaskScheduleModule {

    private static final Logger logger = LoggerFactory.getLogger(TaskScheduleModule.class);
    private MonitorExecutorService scheduler;

    public MonitorExecutorService getScheduler(){
        return scheduler;
    }

    public void initScheduledTask(Map<String, ?> config, String monitorName, AMonitorTaskRunner taskRunner) {
        Map<String, ?> taskSchedule = (Map<String, ?>) config.get("taskSchedule");
        if (taskSchedule != null) {
            createTaskSchedule(taskSchedule, monitorName, taskRunner);
        } else {
            if (scheduler != null) {
                scheduler.shutdown();
                scheduler = null;
            }
        }
    }

    private void createTaskSchedule(Map<String, ?> taskSchedule, String monitorName, AMonitorTaskRunner taskRunner) {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        int numberOfThreads = YmlUtils.getInt(taskSchedule.get("numberOfThreads"), 1);
        int taskDelaySeconds = YmlUtils.getInt(taskSchedule.get("taskDelaySeconds"), 300);
        int initialDelaySeconds = YmlUtils.getInt(taskSchedule.get("initialDelaySeconds"), 10);
        scheduler = new MonitorThreadPoolExecutor(createScheduledThreadPool(numberOfThreads));
        scheduler.scheduleAtFixedRate("" + monitorName + " ScheduledTaskRunner",taskRunner, initialDelaySeconds, taskDelaySeconds, TimeUnit.SECONDS);
        logger.info("Created a Task Scheduler for {} with a delay of {} seconds", taskRunner, taskDelaySeconds);
    }

    private ScheduledThreadPoolExecutor createScheduledThreadPool(int numberOfThreads) {
        return new ScheduledThreadPoolExecutor(numberOfThreads);
    }
}
