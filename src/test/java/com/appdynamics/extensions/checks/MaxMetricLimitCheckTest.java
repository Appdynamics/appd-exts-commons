package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MaxMetricLimitCheck.class, PathResolver.class})
public class MaxMetricLimitCheckTest {

    private Logger logger = Mockito.mock(Logger.class);


    @Test
    public void testMaxMetricLimitReached() throws Exception {
        
        PowerMockito.mockStatic(PathResolver.class);
        Mockito.when(PathResolver.resolveDirectory(AManagedMonitor.class)).thenReturn(new File("src/test/resources"));

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        MaxMetricLimitCheck maxMetricLimitCheck = new MaxMetricLimitCheck(5, TimeUnit.SECONDS, logger);
        maxMetricLimitCheck.check();

        Mockito.verify(logger, Mockito.times(2)).error(logCaptor.capture());

        List<String> allValues = logCaptor.getAllValues();

        String value = allValues.get(0);
        Assert.assertEquals(value, "Found metric limit reached error, below are the details");
    }
}
