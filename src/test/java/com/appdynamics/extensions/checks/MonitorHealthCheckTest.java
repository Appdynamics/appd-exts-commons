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

package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.executorservice.MonitorThreadPoolExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */

@RunWith(MockitoJUnitRunner.class)
public class MonitorHealthCheckTest {


    private MonitorHealthCheck monitorHealthCheck;

    @Mock
    private MonitorExecutorService executorService;

    @Test
    public void testRegisteredChecksForRunOnce() {

        monitorHealthCheck = new MonitorHealthCheck(executorService);
        TestCheck testCheck = new TestCheck();
        TestCheck spyTestCheck = Mockito.spy(testCheck);

        monitorHealthCheck.registerChecks(spyTestCheck);

        monitorHealthCheck.run();

        Mockito.verify(spyTestCheck).check();
    }

    @Test
    public void testRegisteredChecksForRunAlways() throws InterruptedException {

        MonitorThreadPoolExecutor monitorThreadPoolExecutor = new MonitorThreadPoolExecutor((ThreadPoolExecutor) Executors.newScheduledThreadPool(1));

        monitorHealthCheck = new MonitorHealthCheck(monitorThreadPoolExecutor);
        TestCheckAlways testCheck = new TestCheckAlways();
        TestCheckAlways spyTestCheck = Mockito.spy(testCheck);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                Runnable task = (Runnable) invocation.getArguments()[1];
                task.run();

                return null;
            }
        }).when(executorService).scheduleAtFixedRate(Matchers.anyString(), Matchers.any(Runnable.class), Matchers.anyInt(), Matchers.anyInt(), Matchers.any(TimeUnit.class));

        monitorHealthCheck.registerChecks(spyTestCheck);

        monitorHealthCheck.run();

        Thread.currentThread().sleep(4000);

        Mockito.verify(spyTestCheck, Mockito.times(3)).check();
    }

    private class TestCheck implements RunOnceCheck {


        @Override
        public void check() {
            //NO-OP
        }
    }

    private class TestCheckAlways implements RunAlwaysCheck {

        boolean shouldStop = false;
        int i = 0;

        @Override
        public void check() {

            if (!shouldStop) {
                i++;
                if (i == 2) {
                    shouldStop = true;
                }
            }
        }

        @Override
        public int getPeriod() {
            return 2;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return TimeUnit.SECONDS;
        }

        @Override
        public boolean shouldStop() {
            return shouldStop;
        }
    }
}