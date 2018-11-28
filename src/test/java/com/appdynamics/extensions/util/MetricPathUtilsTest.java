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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.conf.modules.MetricCharSequenceReplaceModule;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class MetricPathUtilsTest {
    private String metricPath = "Server|Server1|Queue|Q1|hits";
    private String metricPrefix = "Custom Metrics|Server1";

    @Test
    public void getMetricNameTest(){
        String metricName = MetricPathUtils.getMetricName(metricPath);
        Assert.assertTrue(metricName.equals("hits"));
    }

    @Test
    public void getNullMetricNameTest(){
        String metricName = MetricPathUtils.getMetricName(null);
        Assert.assertTrue(metricName == null);
    }

    @Test
    public void buildMetricPathWithNullMetricReplacementTest() {
        String[] suffixes = new String[] {"North Americas", "Queue1", "Messages"};
        String path = MetricPathUtils.buildMetricPath(metricPrefix, suffixes);
        Assert.assertEquals("Custom Metrics|Server1|North Americas|Queue1|Messages", path);
    }

    @Test
    public void buildMetricPathWithMetricReplacementTest() {
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        when(configuration.getContext()).thenReturn(context);
        MetricPathUtils.registerMetricCharSequenceReplacer(aBaseMonitor);
        MetricCharSequenceReplaceModule metricCharSequenceReplaceModule = new MetricCharSequenceReplaceModule();
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/metricReplace/config_with_non_ascii.yml"));
        metricCharSequenceReplaceModule.initMetricCharSequenceReplacer(conf);
        when(context.getMetricCharSequenceReplacer()).thenReturn(metricCharSequenceReplaceModule.getMetricCharSequenceReplacer());
        String[] suffixes = new String[] {"Español", "Qûeue1", "Messagés"};
        String path = MetricPathUtils.buildMetricPath(metricPrefix, suffixes);
        Assert.assertEquals("Custom Metrics|Server1|Espanol|Queue1|Messages", path);
    }
}
