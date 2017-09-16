package com.appdynamics.extensions.metrics.derived;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.MetricWriteHelperFactory;
import com.google.common.collect.Lists;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class DerivedMetricsTest {
    private class TaskRunner implements Runnable{
        public void run(){

        }
    }

    AManagedMonitor aManagedMonitor = new AManagedMonitor() {
            @Override
            public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
                return null;
            }
        };

    @Test
    public void noDerivedMetricsSectionTest(){
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config_NoDerivedSection.yml");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(0)).printMetric(pathCaptor.capture());
        Assert.assertTrue(pathCaptor.getAllValues().size() == 0);
    }

    @Test
    public void derivedMetricsTest(){
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config.yml");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(1)).printMetric(pathCaptor.capture());
        Assert.assertTrue(pathCaptor.getValue().size() == 2);
        for(Metric metric : (List<Metric>)pathCaptor.getValue()){
            Assert.assertTrue(metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server1|ratio") || metric.getMetricPath().equals("Server|Component:AppLevels|Custom Metrics|Redis|Server2|ratio"));
        }
    }

    @Test
    public void derivedMetricsTestWithLevelDifferences(){
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config_WithLevelVariants.yml");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q3|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU1|cpuRequests","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|CPU2|cpuRequests","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory1|memoryRequests","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Memory2|memoryRequests","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(1)).printMetric(pathCaptor.capture());
        List<String> derivedList = Lists.newArrayList();
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q3|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgRequests");
        for(Metric metric : (List<Metric>)pathCaptor.getValue()){
            Assert.assertTrue(derivedList.contains(metric.getMetricPath()));
        }
    }

    @Test
    public void derivedMetricsTestWithMultipleMetrics(){
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config_MultipleDerivedMetrics.yml");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q3|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|cpuCalls","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|memoryCalls","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|cpuRead","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|cpuRead","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|memoryRead","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|memoryRead","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q1|memoryRead","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q2|memoryRead","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|cpuWrite","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|cpuWrite","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|memoryWrite","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|memoryWrite","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q1|memoryWrite","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q2|memoryWrite","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(1)).printMetric(pathCaptor.capture());
        List<String> derivedList = Lists.newArrayList();
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q3|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgCalls");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|avgRead");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|avgRead");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q1|avgRead");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q2|avgRead");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgWrite");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|avgWrite");
        for(Metric metric : (List<Metric>)pathCaptor.getValue()){
            Assert.assertTrue(derivedList.contains(metric.getMetricPath()));
        }
    }

    @Test
    public void clusterMetricsTest(){
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
        MetricWriteHelper metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        MonitorConfiguration monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config_WithClusterMetrics.yml");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|hits","2","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q1|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q1|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q2|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q2|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(1)).printMetric(pathCaptor.capture());
        List<String> derivedList = Lists.newArrayList();
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server1|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Server2|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Cluster|Q1|ratio");
        derivedList.add("Server|Component:AppLevels|Custom Metrics|Redis|Cluster|Q2|ratio");
        for(Metric metric : (List<Metric>)pathCaptor.getValue()){
            Assert.assertTrue(derivedList.contains(metric.getMetricPath()));
        }

    }

}
