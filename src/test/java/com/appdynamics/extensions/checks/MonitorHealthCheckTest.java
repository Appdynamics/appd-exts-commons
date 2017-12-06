package com.appdynamics.extensions.checks;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
public class MonitorHealthCheckTest {


    private MonitorHealthCheck monitorHealthCheck;

    @Test
    public void testRegisteredChecksForRunOnce() {

        monitorHealthCheck = new MonitorHealthCheck("TestMonitor", new File("monitors/TestMonitor"));
        TestCheck testCheck = new TestCheck();
        TestCheck spyTestCheck = Mockito.spy(testCheck);

        monitorHealthCheck.registerChecks(spyTestCheck);

        monitorHealthCheck.run();

        Mockito.verify(spyTestCheck).check();
    }

    @Test
    public void testRegisteredChecksForRunAlways() throws InterruptedException {

        monitorHealthCheck = new MonitorHealthCheck("TestMonitor", new File("monitors/TestMonitor"));
        TestCheckAlways testCheck = new TestCheckAlways();
        TestCheckAlways spyTestCheck = Mockito.spy(testCheck);

        monitorHealthCheck.registerChecks(spyTestCheck);

        monitorHealthCheck.run();

        Thread.currentThread().sleep(4000);

        Mockito.verify(spyTestCheck, Mockito.times(2)).check();
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
            i++;
            if (i == 2) {
                shouldStop = true;
            }
        }

        @Override
        public long getPeriod() {
            return 1;
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