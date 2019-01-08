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

package com.appdynamics.extensions.executorservice;

import org.junit.Test;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by venkata.konala on 11/1/17.
 */

/*@RunWith(PowerMockRunner.class)
@PrepareForTest({MonitorThreadPoolExecutor.class, LoggerFactory.class})
@SuppressStaticInitializationFor("com.appdynamics.extensions.executorservice.MonitorThreadPoolExecutor.class")*/
public class MonitorThreadPoolExecutorTest {

    static Logger logger;

    /*@BeforeClass
    public static void init(){
        PowerMockito.mockStatic(LoggerFactory.class);
        logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(ExtensionsLoggerFactory.getLogger(MonitorThreadPoolExecutor.class)).thenReturn(logger);
    }*/

    class sampleRunnable implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {

            }
        }
    }

    @Test
    public void checkIfLoggingHappensIfTaskExceedsThreshold() throws InterruptedException, ExecutionException {
        MonitorThreadPoolExecutor monitorThreadPoolExecutor = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(5));
        monitorThreadPoolExecutor.submit("sampleRunnable", new sampleRunnable());
        Thread.sleep(20);
    }
}
