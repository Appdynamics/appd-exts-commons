package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.AMonitorTaskRunner;
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
public class TaskScheduleModuleTest {

    @Test
    public void noParametersInTaskScheduleReturnsNullSchedulerTest(){
        AMonitorTaskRunner aMonitorTaskRunner = mock(AMonitorTaskRunner.class);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithTaskSchedule.yml"));
        TaskScheduleModule taskScheduleModule = new TaskScheduleModule();
        taskScheduleModule.initScheduledTask(conf, "redis Monitor", aMonitorTaskRunner);
        MonitorExecutorService scheduler = taskScheduleModule.getScheduler();
        Assert.assertTrue(scheduler == null);
    }

    @Test
    public void noTaskScheduleReturnsNullSchedulerTest(){
        AMonitorTaskRunner aMonitorTaskRunner = mock(AMonitorTaskRunner.class);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        TaskScheduleModule taskScheduleModule = new TaskScheduleModule();
        taskScheduleModule.initScheduledTask(conf, "redis Monitor", aMonitorTaskRunner);
        MonitorExecutorService scheduler = taskScheduleModule.getScheduler();
        Assert.assertTrue(scheduler == null);
    }

    @Test
    public void withPartialOrFullParametersInTaskScheduleReturnsSchedulerTest(){
        AMonitorTaskRunner aMonitorTaskRunner = mock(AMonitorTaskRunner.class);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithTaskScheduleParameters.yml"));
        TaskScheduleModule taskScheduleModule = new TaskScheduleModule();
        taskScheduleModule.initScheduledTask(conf, "redis Monitor", aMonitorTaskRunner);
        MonitorExecutorService scheduler = taskScheduleModule.getScheduler();
        Assert.assertTrue(scheduler != null);
    }
}
