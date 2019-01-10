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
    public void testMaxMetricLimitReached() {
        
        PowerMockito.mockStatic(PathResolver.class);
        Mockito.when(PathResolver.resolveDirectory(AManagedMonitor.class)).thenReturn(new File("src/test/resources"));

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        long start = System.currentTimeMillis();
        MaxMetricLimitCheck maxMetricLimitCheck = new MaxMetricLimitCheck(5, TimeUnit.SECONDS, logger);
        maxMetricLimitCheck.check();
        long time = System.currentTimeMillis() - start;

        Mockito.verify(logger, Mockito.times(2)).error(logCaptor.capture());

        List<String> allValues = logCaptor.getAllValues();

        String value = allValues.get(0);
        Assert.assertEquals(value, "Found metric limit reached error, below are the details");
        Assert.assertNotNull(allValues.get(1));
        System.out.println("Took "+time+" ms");
    }
}
