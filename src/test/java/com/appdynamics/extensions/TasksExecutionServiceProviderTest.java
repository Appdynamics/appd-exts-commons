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

import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.conf.modules.MonitorExecutorServiceModule;
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


/**
 * Created by venkata.konala on 11/1/17.
 */

public class TasksExecutionServiceProviderTest {

    class SampleRunnable implements AMonitorTaskRunnable {

        @Override
        public void onTaskComplete() {

        }

        @Override
        public void run() {

        }
    }

    @Test
    public void checkIfOnCompleteMethodsAreCalledAfterTasksSubmittedTest() throws InterruptedException {
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        List<Map<String, ?>> servers = new ArrayList<Map<String, ?>>();
        Map<String, Object> server1 = new HashMap<>();
        server1.put("host","localhost");
        server1.put("port", 8090);

        Map<String, Object> server2 = new HashMap<>();
        server2.put("host","localhost");
        server2.put("port", 8090);

        servers.add(server1);
        servers.add(server2);

        when(aBaseMonitor.getServers()).thenReturn(servers);
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ? super Object> conf = Maps.newHashMap();
        conf.put("numberOfThreads", "10");
        monitorExecutorServiceModule.initExecutorService(conf, "test");
        MonitorExecutorService monitorExecutorService = monitorExecutorServiceModule.getExecutorService();
        MonitorContext context = mock(MonitorContext.class);
        when(configuration.getContext()).thenReturn(context);
        when(context.getExecutorService()).thenReturn(monitorExecutorService);
        MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
        TasksExecutionServiceProvider tasksExecutionServiceProvider = new TasksExecutionServiceProvider(aBaseMonitor, metricWriteHelper);
        //SampleRunnable sampleRunnable = new SampleRunnable();
        tasksExecutionServiceProvider.submit("Task1", new SampleRunnable());
        tasksExecutionServiceProvider.submit("Task2", new SampleRunnable());
        Thread.sleep(1000);
        verify(metricWriteHelper, times(1)).onComplete();
        verify(aBaseMonitor, times(1)).onComplete();
    }

    @Test
    public void checkIfOnCompleteMethodsAreNotCalledAfterTasksSubmittedAreLessThanTaskCountTest() throws InterruptedException {
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);

        List<Map<String, ?>> servers = new ArrayList<Map<String, ?>>();
        Map<String, Object> server1 = new HashMap<>();
        server1.put("host","localhost");
        server1.put("port", 8090);

        Map<String, Object> server2 = new HashMap<>();
        server2.put("host","localhost");
        server2.put("port", 8090);

        Map<String, Object> server3 = new HashMap<>();
        server2.put("host","localhost");
        server2.put("port", 8090);

        servers.add(server1);
        servers.add(server2);
        servers.add(server3);

        when(aBaseMonitor.getServers()).thenReturn(servers);
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ? super Object> conf = Maps.newHashMap();
        conf.put("numberOfThreads", "10");
        monitorExecutorServiceModule.initExecutorService(conf, "test");
        MonitorExecutorService monitorExecutorService = monitorExecutorServiceModule.getExecutorService();
        MonitorContext context = mock(MonitorContext.class);
        when(configuration.getContext()).thenReturn(context);
        when(context.getExecutorService()).thenReturn(monitorExecutorService);
        MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
        TasksExecutionServiceProvider tasksExecutionServiceProvider = new TasksExecutionServiceProvider(aBaseMonitor, metricWriteHelper);
        //SampleRunnable sampleRunnable = new SampleRunnable();
        tasksExecutionServiceProvider.submit("Task1", new SampleRunnable());
        tasksExecutionServiceProvider.submit("Task2", new SampleRunnable());
        Thread.sleep(1000);
        verify(metricWriteHelper, times(0)).onComplete();
        verify(aBaseMonitor, times(0)).onComplete();
    }



}

