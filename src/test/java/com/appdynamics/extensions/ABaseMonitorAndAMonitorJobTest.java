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

package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.ListMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 11/1/17.
 */
public class ABaseMonitorAndAMonitorJobTest {

   ABaseMonitor aBaseMonitor;
   MonitorContextConfiguration configuration;
    @Before
    public void setUp(){
        aBaseMonitor = mock(ABaseMonitor.class);
        configuration = mock(MonitorContextConfiguration.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
    }


    public class sampleTaskRunnable implements AMonitorTaskRunnable{

        private MetricWriteHelper metricWriteHelper;
        private String value;
        public sampleTaskRunnable(MetricWriteHelper metricWriteHelper, String value){
            this.metricWriteHelper = metricWriteHelper;
            this.value = value;
        }

        @Override
        public void onTaskComplete() {

        }

        @Override
        public void run(){
            metricWriteHelper.printMetric("Custom Metric|Sample|sample value", value, "AVERAGE", "AVERAGE", "INDIVIDUAL");
        }
    }

    public class SampleMonitor extends ABaseMonitor{

        @Override
        protected String getDefaultMetricPrefix() {
            return "Custom Metrics|Sample";
        }

        @Override
        public String getMonitorName() {
            return "Sample Monitor";
        }

        @Override
        protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {

            MetricWriteHelper metricWriteHelper = tasksExecutionServiceProvider.getMetricWriteHelper();
            metricWriteHelper.setCacheMetrics(true);
            tasksExecutionServiceProvider.submit("sample task 1", new sampleTaskRunnable(metricWriteHelper, "4"));
            tasksExecutionServiceProvider.submit("sample task 2", new sampleTaskRunnable(metricWriteHelper, "3"));
        }

        @Override
        protected List<Map<String, ?>> getServers() {
           return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
        }
    }


    @Test
    public void sampleRun() throws TaskExecutionException{
        SampleMonitor sampleMonitor = new SampleMonitor();
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/config.yml");
        sampleMonitor.execute(args, null);
    }


    @Test
    public void sampleRunWithDisplayNameCheckNotEnabled() throws TaskExecutionException{
        SampleMonitor sampleMonitor = new SampleMonitor();
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/config_WithDisplayNameCheckNotEnabled.yml");
        sampleMonitor.execute(args, null);
    }

    @Test
    public void cacheMetricsTest() throws TaskExecutionException, InterruptedException{
        SampleMonitor sampleMonitor = new SampleMonitor();
        Map<String, String> args = Maps.newHashMap();
        configuration.setConfigYml("src/test/resources/conf/config.yml");
        args.put("config-file", "src/test/resources/conf/config.yml");
        sampleMonitor.execute(args, null);
        Thread.sleep(1000);
        ConcurrentMap<String, Metric> cache = sampleMonitor.getContextConfiguration().getContext().getCachedMetrics();
        Assert.assertTrue(cache.get("Custom Metric|Sample|sample value").getMetricValue().equals("3") || cache.get("Custom Metric|Sample|sample value").getMetricValue().equals("4"));
    }

    public class TestMonitorWithFanOut extends ABaseMonitor{

        @Override
        protected String getDefaultMetricPrefix() {
            return "Custom Metrics|TestMonitorWithFanOut";
        }

        @Override
        public String getMonitorName() {
            return "Test Monitor With FanOut";
        }

        @Override
        protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) { }

        @Override
        protected List<Map<String, ?>> getServers() {
          return  (List<Map<String, ?>>) getContextConfiguration().
                    getConfigYml().get("servers");
        }
    }


    @Test
    public void whenMultipleServersThenCheckDisplayNamePresent() throws TaskExecutionException, InterruptedException{
        TestMonitorWithFanOut testMonitor = new TestMonitorWithFanOut();
        Map<String, String> args = Maps.newHashMap();

        configuration.setConfigYml("src/test/resources/conf/config_WithMultipleServersDisplayNameCheck.yml");
        args.put("config-file", "src/test/resources/conf/config_WithMultipleServersDisplayNameCheck.yml");
        testMonitor.execute(args, null);
        Thread.sleep(1000);
    }

    @Test(expected = RuntimeException.class)
    public void whenMultipleServersAndDisplayNameNotPresentThenThrowException() throws TaskExecutionException, InterruptedException{
        TestMonitorWithFanOut testMonitorWithFanOut = new TestMonitorWithFanOut();
        Map<String, String> args = Maps.newHashMap();
        configuration.setConfigYml("src/test/resources/conf/config_WithMultipleServersDisplayNameAbsent.yml");
        args.put("config-file", "src/test/resources/conf/config_WithMultipleServersDisplayNameAbsent.yml");
        testMonitorWithFanOut.execute(args, null);
        Thread.sleep(1000);
    }

    @Test
    public void whenDisplayNameFlagNullThenDefaultToTrue() throws TaskExecutionException, InterruptedException{
        TestMonitorWithFanOut testMonitorWithFanOut = new TestMonitorWithFanOut();
        Map<String, String> args = Maps.newHashMap();
        configuration.setConfigYml("src/test/resources/conf/config_WithMultipleServersDisplayNameCheck.yml");
        args.put("config-file", "src/test/resources/conf/config_WithMultipleServersDisplayNameCheck.yml");
        testMonitorWithFanOut.execute(args, null);
        Thread.sleep(1000);
    }


    public class TestMonitorWithoutFanOut extends ABaseMonitor{

        @Override
        protected String getDefaultMetricPrefix() {
            return "Custom Metrics|TestMonitorWithoutFanOut";
        }

        @Override
        public String getMonitorName() {
            return "Test Monitor Without FanOut";
        }

        @Override
        protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) { }

        @Override
        protected List<Map<String, ?>> getServers() {
            return  (List<Map<String, ?>>) getContextConfiguration().
                    getConfigYml().get("servers");
        }
    }

    @Test
    public void whenOneServerThenCheckDisplayNameNotPresent() throws TaskExecutionException, InterruptedException{
        TestMonitorWithoutFanOut testMonitorWithoutFanOut = new TestMonitorWithoutFanOut();
        Map<String, String> args = Maps.newHashMap();
        configuration.setConfigYml("src/test/resources/conf/config_WithOneServerDisplayNameCheck.yml");
        args.put("config-file", "src/test/resources/conf/config_WithOneServerDisplayNameCheck.yml");
        testMonitorWithoutFanOut.execute(args, null);
        Thread.sleep(1000);
    }

    @Test(expected = RuntimeException.class )
    public void whenOneServerAndDisplayNamePresentThenThrowException() throws TaskExecutionException, InterruptedException{
        TestMonitorWithoutFanOut testMonitorWithoutFanOut = new TestMonitorWithoutFanOut();
        Map<String, String> args = Maps.newHashMap();
        configuration.setConfigYml("src/test/resources/conf/config_WithOneServerDisplayNamePresent.yml");
        args.put("config-file", "src/test/resources/conf/config_WithOneServerDisplayNamePresent.yml");
        testMonitorWithoutFanOut.execute(args, null);
        Thread.sleep(1000);
    }



}
