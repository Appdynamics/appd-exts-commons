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
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class JobScheduleModuleTest {

    @Test
    public void noParametersInTaskScheduleReturnsNullSchedulerTest(){
        AMonitorJob aMonitorJob = mock(AMonitorJob.class);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithTaskSchedule.yml"));
        JobScheduleModule jobScheduleModule = new JobScheduleModule();
        jobScheduleModule.initScheduledJob(conf, "redis Monitor", aMonitorJob);
        MonitorExecutorService scheduler = jobScheduleModule.getScheduler();
        Assert.assertTrue(scheduler == null);
    }

    @Test
    public void noTaskScheduleReturnsNullSchedulerTest(){
        AMonitorJob aMonitorJob = mock(AMonitorJob.class);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        JobScheduleModule jobScheduleModule = new JobScheduleModule();
        jobScheduleModule.initScheduledJob(conf, "redis Monitor", aMonitorJob);
        MonitorExecutorService scheduler = jobScheduleModule.getScheduler();
        Assert.assertTrue(scheduler == null);
    }

    @Test
    public void withPartialOrFullParametersInTaskScheduleReturnsSchedulerTest(){
        AMonitorJob aMonitorJob = mock(AMonitorJob.class);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithTaskScheduleParameters.yml"));
        JobScheduleModule jobScheduleModule = new JobScheduleModule();
        jobScheduleModule.initScheduledJob(conf, "redis Monitor", aMonitorJob);
        MonitorExecutorService scheduler = jobScheduleModule.getScheduler();
        Assert.assertTrue(scheduler != null);
    }
}
