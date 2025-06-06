/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

package com.appdynamics.extensions;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * TaskExecutionServiceProvider is responsible for submitting all the tasks of
 * a job to the ExecutorService and also making sure that all the job tasks are
 * completed before executing the onComplete() method of MetricWriteHelper.
 */
public class TasksExecutionServiceProvider {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(TasksExecutionServiceProvider.class);
    private ABaseMonitor aBaseMonitor;
    private AtomicInteger taskCounter;
    private MetricWriteHelper metricWriteHelper;


    public TasksExecutionServiceProvider(ABaseMonitor aBaseMonitor, MetricWriteHelper metricWriteHelper) {
        this.aBaseMonitor = aBaseMonitor;
        this.metricWriteHelper = metricWriteHelper;
        taskCounter = new AtomicInteger(this.aBaseMonitor.getServers().size());
    }


    public void submit(final String name, final AMonitorTaskRunnable aServerTask) {
        aBaseMonitor.getContextConfiguration().getContext().getExecutorService().submit(name, new Runnable() {
            @Override
            public void run() {
                try {
                    aServerTask.run();
                    aServerTask.onTaskComplete();
                } catch (Throwable e) {
                    logger.error("Unforeseen error or exception happened", e);
                } finally {
                    if (taskCounter.decrementAndGet() <= 0) {
                        onRunComplete();
                    }
                }
            }
        });
    }

    private void onRunComplete() {
        metricWriteHelper.onComplete();
        aBaseMonitor.onComplete();
    }

    public MetricWriteHelper getMetricWriteHelper() {
        return this.metricWriteHelper;
    }
}
