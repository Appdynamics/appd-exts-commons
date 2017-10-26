package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by venkata.konala on 10/24/17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MonitorExecutorServiceModule.class, LoggerFactory.class})
@SuppressStaticInitializationFor("com.appdynamics.extensions.conf.modules.MonitorExecutorServiceModule.class")
public class MonitorExecutorServiceModuleTest {

    static Logger logger;

    class dummyThread implements Runnable{
        public void run(){
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException ie){

            }
        }
    }

    @BeforeClass
    public static void init(){
        PowerMockito.mockStatic(LoggerFactory.class);
        logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(LoggerFactory.getLogger(MonitorExecutorServiceModule.class)).thenReturn(logger);
    }

    @Test
    public void differentNumberOfThreadsWillTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
        Map<String, ?> conf2 = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithDifferentThreadNumber.yml"));
        monitorExecutorServiceModule.initExecutorService(conf2);
        MonitorExecutorService executorService2 = monitorExecutorServiceModule.getExecutorService();
        Assert.assertTrue(executorService != executorService2);
    }


    @Test
    public void sameNumberOfThreadsTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
        Map<String, ?> conf2 = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithSameThreadNumber.yml"));
        monitorExecutorServiceModule.initExecutorService(conf2);
        MonitorExecutorService executorService2 = monitorExecutorServiceModule.getExecutorService();
        Assert.assertTrue(executorService == executorService2);
    }

    @Test(expected = RuntimeException.class)
    public void numberOfThreadsNotPresentTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithNoThreadNumber.yml"));
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
    }

    @Test
    public void queueCapacityReachedPrintsLogStatementTest() throws InterruptedException {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithQueueCapacityGrowthFactor.yml"));
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
        dummyThread t1 = new dummyThread();
        dummyThread t2 = new dummyThread();
        dummyThread t3 = new dummyThread();
        dummyThread t4 = new dummyThread();
        executorService.submit("task1", t1);
        executorService.submit("task2", t2);
        executorService.submit("task3", t3);
        executorService.submit("task4", t4);
        Thread.sleep(2000);
        //verify(logger,atLeastOnce()).error(anyString());
        verify(logger,atLeastOnce()).error("Queue Capacity reached!! Rejecting runnable tasks..");
    }
}
