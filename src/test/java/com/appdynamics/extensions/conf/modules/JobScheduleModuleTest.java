package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MonitorExecutorService;
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
