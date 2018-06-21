package com.appdynamics.extensions.checks;

import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.junit.Assert;
import org.junit.Ignore;
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
 * @author Akshay Srivastava
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MetricBlacklistLimitCheck.class, PathResolver.class})
public class MetricBlacklistLimitCheckTest {


    private Logger logger = Mockito.mock(Logger.class);


    @Test
    public void testBlacklistMetricLimitReached() {

        PowerMockito.mockStatic(PathResolver.class);
        Mockito.when(PathResolver.resolveDirectory(AManagedMonitor.class)).thenReturn(new File("src/test/resources"));

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        long start = System.currentTimeMillis();
        MetricBlacklistLimitCheck maxMetricLimitCheck = new MetricBlacklistLimitCheck(5, TimeUnit.SECONDS, logger);
        maxMetricLimitCheck.check();
        long time = System.currentTimeMillis() - start;

        Mockito.verify(logger, Mockito.times(2)).error(logCaptor.capture());

        List<String> allValues = logCaptor.getAllValues();

        String value = allValues.get(0);
        Assert.assertEquals(value, "Found blacklist metric limit reached error, below are the details");
        Assert.assertNotNull(allValues.get(1));
        System.out.println("Took "+time+" ms");
    }
}
