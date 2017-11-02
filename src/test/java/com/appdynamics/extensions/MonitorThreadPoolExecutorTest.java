package com.appdynamics.extensions;

import org.junit.Test;
import org.slf4j.Logger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by venkata.konala on 11/1/17.
 */

/*@RunWith(PowerMockRunner.class)
@PrepareForTest({MonitorThreadPoolExecutor.class, LoggerFactory.class})
@SuppressStaticInitializationFor("com.appdynamics.extensions.MonitorThreadPoolExecutor.class")*/
public class MonitorThreadPoolExecutorTest {

    static Logger logger;

    /*@BeforeClass
    public static void init(){
        PowerMockito.mockStatic(LoggerFactory.class);
        logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(LoggerFactory.getLogger(MonitorThreadPoolExecutor.class)).thenReturn(logger);
    }*/

    class sampleRunnable implements  Runnable{

        @Override
        public void run() {
            try {
                Thread.sleep(10);
            }
            catch(InterruptedException ie){

            }
        }
    }

    @Test
    public void checkIfLoggingHappensIfTaskExceedsThreshold() throws  InterruptedException, ExecutionException{
        MonitorThreadPoolExecutor monitorThreadPoolExecutor = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(5));
        monitorThreadPoolExecutor.submit("sampleRunnable", new sampleRunnable());
        Thread.sleep(20);
    }
}
