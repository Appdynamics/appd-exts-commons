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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author pradeep.nair
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MetricCharSequenceReplaceModule.class, MetricCharSequenceReplacer.class})
public class MetricCharSequenceReplaceModuleTest {

    private MetricCharSequenceReplaceModule replaceModule;

    @Mock
    private MetricCharSequenceReplacer replacer;

    @Before
    public void setup() {
        replaceModule = new MetricCharSequenceReplaceModule();
    }

    @Test
    public void verifyReplacerWhenMonitorContextIsInitialized() {
        PowerMockito.mockStatic(MetricCharSequenceReplacer.class);
        when(MetricCharSequenceReplacer.createInstance(anyMapOf(String.class, String.class))).thenReturn(replacer);
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config.yml"));
        replaceModule.initMetricCharSequenceReplacer(conf);
        verify(replacer);
    }
}
