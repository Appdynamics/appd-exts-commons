package com.appdynamics.extensions.util.derived;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class DerivedMetricsTest {
    private MonitorConfiguration monitorConfiguration;
    private class TaskRunner implements Runnable{
        public void run(){

        }
    }
    private MetricWriteHelper metricWriteHelper;
    AManagedMonitor aManagedMonitor = new AManagedMonitor() {
            @Override
            public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
                return null;
            }
        };

    @Test
    public void noDerivedMetricsSectionTest(){
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config_NoDerivedSection.yml");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(0)).printMetric(pathCaptor.capture(), anyString(), anyString(), anyString(), anyString());
        Assert.assertTrue(pathCaptor.getAllValues().size() == 0);
    }

    @Test
    public void derivedMetricsTest(){
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
        monitorConfiguration.setConfigYml("src/test/resources/derived/config.yml");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server1|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|hits","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.printMetric("Server|Component:AppLevels|Custom Metrics|Redis|Server2|misses","1","AVERAGE","AVERAGE", "INDIVIDUAL");
        metricWriteHelper.onTaskComplete();
        verify(metricWriteHelper, times(6)).printMetric(pathCaptor.capture(), anyString(), anyString(), anyString(), anyString());
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|ratio"));
    }

    @Test
    public void derivedMetricsTestWithLevelDifferences(){
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
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
        verify(metricWriteHelper, times(18)).printMetric(pathCaptor.capture(), anyString(), anyString(), anyString(), anyString());
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q3|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgRequests"));
    }

    @Test
    public void derivedMetricsTestWithMultipleMetrics(){
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
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
        verify(metricWriteHelper, times(33)).printMetric(pathCaptor.capture(), anyString(), anyString(), anyString(), anyString());
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q3|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q4|ratio"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgCalls"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q1|avgRead"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|Q2|avgRead"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q1|avgRead"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|Q2|avgRead"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgWrite"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server1|avgWrite"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|avgWrite"));
        Assert.assertTrue(pathCaptor.getAllValues().contains("Server|Component:AppLevels|Custom Metrics|Redis|Server2|avgWrite"));
    }

    @Test
    public void clusterMetricsTest(){
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        metricWriteHelper = Mockito.spy(MetricWriteHelperFactory.create(aManagedMonitor));
        monitorConfiguration = new MonitorConfiguration("Custom Metrics|Redis|", new TaskRunner(), metricWriteHelper);
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
        verify(metricWriteHelper, times(16)).printMetric(pathCaptor.capture(), anyString(), anyString(), anyString(), anyString());
        Assert.assertTrue(Collections.frequency(pathCaptor.getAllValues(), "Server|Component:AppLevels|Custom Metrics|Redis|Server1|ratio") == 2);
        Assert.assertTrue(Collections.frequency(pathCaptor.getAllValues(), "Server|Component:AppLevels|Custom Metrics|Redis|Server2|ratio") == 2);
        Assert.assertTrue(Collections.frequency(pathCaptor.getAllValues(), "Server|Component:AppLevels|Custom Metrics|Redis|Cluster|Q1|ratio") == 2);
        Assert.assertTrue(Collections.frequency(pathCaptor.getAllValues(), "Server|Component:AppLevels|Custom Metrics|Redis|Cluster|Q2|ratio") == 2);
    }

}
