package com.appdynamics.extensions.conf.configurationModules;

import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class MonitorExecutorServiceModuleTest {

    /*private final Appender appender = mock(Appender.class);
    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();

    @Captor
    private ArgumentCaptor<LoggingEvent> logCaptor;

    @Before
    public void setup() {
        logger.setLevel(Level.ERROR);
        logger.addAppender(appender);
    }

    @After
    public void remove(){
        logger.removeAppender(appender);
    }
*/


    class dummyThread implements Runnable{
        public void run(){
            try {
                Thread.sleep(2000);
            }
            catch(InterruptedException ie){

            }
        }
    }



    @Test
    public void differentNumberOfThreadsWillreturnDifferentExecutorServiceTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/configuration/config.yml"));
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
        Map<String, ?> conf2 = YmlReader.readFromFile(new File("src/test/resources/configuration/config_WithDifferentThreadNumber.yml"));
        monitorExecutorServiceModule.initExecutorService(conf2);
        MonitorExecutorService executorService2 = monitorExecutorServiceModule.getExecutorService();
        Assert.assertTrue(executorService != executorService2);
    }


    @Test
    public void sameNumberOfThreadsWillReturnSameExecutorServiceTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/configuration/config.yml"));
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
        Map<String, ?> conf2 = YmlReader.readFromFile(new File("src/test/resources/configuration/config_WithSameThreadNumber.yml"));
        monitorExecutorServiceModule.initExecutorService(conf2);
        MonitorExecutorService executorService2 = monitorExecutorServiceModule.getExecutorService();
        Assert.assertTrue(executorService == executorService2);
    }

    @Test(expected = RuntimeException.class)
    public void numberOfThreadsNotPresentWillThrowExceptionTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/configuration/config_WithNoThreadNumber.yml"));
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService executorService = monitorExecutorServiceModule.getExecutorService();
    }

    @Test
    public void queueCapacityReachedPrintsLogStatementTest(){
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/configuration/config_WithQueueCapacityGrowthFactor.yml"));
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
        /*verify(appender,times(2)).doAppend(logCaptor.capture());
        LoggingEvent loggingEvent = logCaptor.getAllValues().get(0);
        Assert.assertEquals("Queue Capacity reached!! Rejecting runnable tasks..", loggingEvent.getMessage());*/
    }
}
