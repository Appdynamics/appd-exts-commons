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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.YmlUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class JobScheduleModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(JobScheduleModule.class);
    private MonitorExecutorService scheduler;

    public MonitorExecutorService getScheduler() {
        return scheduler;
    }

    public void initScheduledJob(Map<String, ?> config, String monitorName, AMonitorJob taskRunner) {
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

    private void createTaskSchedule(Map<String, ?> taskSchedule, String monitorName, AMonitorJob monitorJob) {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        int numberOfThreads = YmlUtils.getInt(taskSchedule.get("numberOfThreads"), 1);
        int taskDelaySeconds = YmlUtils.getInt(taskSchedule.get("taskDelaySeconds"), 300);
        int initialDelaySeconds = YmlUtils.getInt(taskSchedule.get("initialDelaySeconds"), 10);
        scheduler = new MonitorThreadPoolExecutor(createScheduledThreadPool(numberOfThreads), monitorName);
        scheduler.scheduleAtFixedRate("" + monitorName + " ScheduledTaskRunner", monitorJob, initialDelaySeconds, taskDelaySeconds, TimeUnit.SECONDS);
        logger.info("Created a Task Scheduler for {} with a delay of {} seconds", monitorJob, taskDelaySeconds);
    }

    private ScheduledThreadPoolExecutor createScheduledThreadPool(int numberOfThreads) {
        return new ScheduledThreadPoolExecutor(numberOfThreads);
    }
}
