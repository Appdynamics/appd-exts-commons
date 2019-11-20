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

package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.conf.modules.KubernetesDiscoveryModule;
import com.appdynamics.extensions.discovery.k8s.KubernetesDiscoveryService;
import com.appdynamics.extensions.discovery.k8s.PodAddress;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by venkata.konala on 11/1/17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ABaseMonitor.class, KubernetesDiscoveryModule.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class ABaseMonitorAndAMonitorJobTest {

    private ABaseMonitor aBaseMonitor;
    private MonitorContextConfiguration configuration;

    @Mock
    private KubernetesDiscoveryService kubernetesDiscoveryService;
    @Mock
    private KubernetesDiscoveryService.KubernetesDiscoveryBuilder kubernetesDiscoveryBuilder;

    @Before
    public void setUp() {
        aBaseMonitor = mock(ABaseMonitor.class);
        configuration = mock(MonitorContextConfiguration.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
    }


    public class sampleTaskRunnable implements AMonitorTaskRunnable {

        private MetricWriteHelper metricWriteHelper;
        private String value;
        private String path;

        public sampleTaskRunnable(MetricWriteHelper metricWriteHelper, String path, String value) {
            this.metricWriteHelper = metricWriteHelper;
            this.value = value;
            this.path = path;
        }

        @Override
        public void onTaskComplete() {

        }

        @Override
        public void run() {
            metricWriteHelper.printMetric(path, value, "AVERAGE", "AVERAGE", "INDIVIDUAL");
        }
    }

    public class SampleMonitor extends ABaseMonitor {

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
            tasksExecutionServiceProvider.submit("sample task 1", new sampleTaskRunnable(metricWriteHelper, "Custom Metrics|Sample|sample value", "4"));
            tasksExecutionServiceProvider.submit("sample task 2", new sampleTaskRunnable(metricWriteHelper, "Custom Metrics|Sample|sample value", "3"));
        }

        @Override
        protected List<Map<String, ?>> getServers() {
            return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
        }
    }

    public class SampleMonitorK8S extends ABaseMonitor {

        private TasksExecutionServiceProvider tasksExecutionServiceProvider;

        public TasksExecutionServiceProvider getTasksExecutionServiceProvider() {
            return tasksExecutionServiceProvider;
        }

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
            this.tasksExecutionServiceProvider = tasksExecutionServiceProvider;

            List<Map<String, ?>> servers = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");


            MetricWriteHelper metricWriteHelper = tasksExecutionServiceProvider.getMetricWriteHelper();
            metricWriteHelper.setCacheMetrics(true);
            tasksExecutionServiceProvider.submit("sample task 1", new sampleTaskRunnable(metricWriteHelper, "Custom Metrics|Sample|K8S Servers", servers.size() + ""));
        }

        @Override
        protected List<Map<String, ?>> getServers() {
            return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
        }
    }


    @Test
    public void sampleRun() throws TaskExecutionException {
        SampleMonitor sampleMonitor = new SampleMonitor();
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/config.yml");
        sampleMonitor.execute(args, null);
    }

    @Test
    public void sampleRunK8SEnabled() throws Exception {

        PowerMockito.whenNew(KubernetesDiscoveryService.KubernetesDiscoveryBuilder.class).withNoArguments().thenReturn(kubernetesDiscoveryBuilder);

        Mockito.when(kubernetesDiscoveryBuilder.withNamespace(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withContainerImage(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withContainerPortName(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withLabels(Matchers.anyMap())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.build()).thenReturn(kubernetesDiscoveryService);

        Mockito.when(kubernetesDiscoveryService.discover()).thenReturn(Lists.newArrayList(new PodAddress("ip1", 80)));

        SampleMonitorK8S sampleMonitor = new SampleMonitorK8S();
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/k8s/config.yml");
        sampleMonitor.execute(args, null);
    }

    @Test
    public void sampleRunK8SEnabledWithUpdatedPods() throws Exception {

        PowerMockito.whenNew(KubernetesDiscoveryService.KubernetesDiscoveryBuilder.class).withNoArguments().thenReturn(kubernetesDiscoveryBuilder);

        Mockito.when(kubernetesDiscoveryBuilder.withNamespace(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withContainerImage(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withContainerPortName(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withLabels(Matchers.anyMap())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.build()).thenReturn(kubernetesDiscoveryService);

        Mockito.when(kubernetesDiscoveryService.discover()).thenReturn(Lists.newArrayList(new PodAddress("ip1", 80)));


        SampleMonitorK8S sampleMonitor = new SampleMonitorK8S();
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/k8s/config.yml");
        sampleMonitor.execute(args, null);

        List<Map<String, ?>> servers = (List<Map<String, ?>>) sampleMonitor.getContextConfiguration().getConfigYml().get("servers");

        Assert.assertEquals("Servers should be 1", 1, servers.size());


        Thread.sleep(3000);

        Mockito.when(kubernetesDiscoveryService.discover()).thenReturn(Lists.newArrayList(new PodAddress("ip1", 80), new PodAddress("ip2", 80)));
        sampleMonitor.execute(args, null);

        servers = (List<Map<String, ?>>) sampleMonitor.getContextConfiguration().getConfigYml().get("servers");
        Assert.assertEquals("Servers should be 1", 1, servers.size());


        Thread.sleep(3000);
        sampleMonitor.execute(args, null);

        servers = (List<Map<String, ?>>) sampleMonitor.getContextConfiguration().getConfigYml().get("servers");
        Assert.assertEquals("Servers should be 2", 2, servers.size());
    }

    @Test
    public void cacheMetricsTest() throws TaskExecutionException, InterruptedException {
        SampleMonitor sampleMonitor = new SampleMonitor();
        Map<String, String> args = Maps.newHashMap();
        configuration.loadConfigYml("src/test/resources/conf/config.yml");
        args.put("config-file", "src/test/resources/conf/config.yml");
        sampleMonitor.execute(args, null);
        Thread.sleep(1000);
        ConcurrentMap<String, Metric> cache = sampleMonitor.getContextConfiguration().getContext().getCachedMetrics();
        Assert.assertTrue(cache.get("Custom Metrics|Sample|sample value").getMetricValue().equals("3") || cache.get("Custom Metrics|Sample|sample value").getMetricValue().equals("4"));
    }


}
