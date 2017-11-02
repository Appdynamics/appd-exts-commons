package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.conf.modules.MonitorExecutorServiceModule;
import com.google.common.collect.Maps;
import org.junit.Test;
import java.util.Map;
import static org.mockito.Mockito.*;

/**
 * Created by venkata.konala on 11/1/17.
 */
public class TasksExecutionServiceProviderTest {

    class SampleRunnable implements AMonitorTaskRunnable{

        @Override
        public void onTaskComplete() {

        }

        @Override
        public void run() {

        }
    }
    @Test
    public void checkIfOnCompleteMethodsAreCalledAfterTasksSubmittedTest() throws InterruptedException{
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        when(aBaseMonitor.getTaskCount()).thenReturn(2);
        MonitorConfiguration configuration = mock(MonitorConfiguration.class);
        when(aBaseMonitor.getConfiguration()).thenReturn(configuration);
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ? super Object> conf = Maps.newHashMap();
        conf.put("numberOfThreads", "10");
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService monitorExecutorService = monitorExecutorServiceModule.getExecutorService();
        when(configuration.getExecutorService()).thenReturn(monitorExecutorService);
        MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
        TasksExecutionServiceProvider tasksExecutionServiceProvider =  new TasksExecutionServiceProvider(aBaseMonitor, metricWriteHelper);
        //SampleRunnable sampleRunnable = new SampleRunnable();
        tasksExecutionServiceProvider.submit("Task1", new SampleRunnable());
        tasksExecutionServiceProvider.submit("Task2", new SampleRunnable());
        Thread.sleep(1000);
        verify(metricWriteHelper, times(1)).onComplete();
        verify(aBaseMonitor, times(1)).onComplete();
    }

    @Test
    public void checkIfOnCompleteMethodsAreNotCalledAfterTasksSubmittedAreLessThanTaskCountTest() throws  InterruptedException{
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        when(aBaseMonitor.getTaskCount()).thenReturn(3);
        MonitorConfiguration configuration = mock(MonitorConfiguration.class);
        when(aBaseMonitor.getConfiguration()).thenReturn(configuration);
        MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        Map<String, ? super Object> conf = Maps.newHashMap();
        conf.put("numberOfThreads", "10");
        monitorExecutorServiceModule.initExecutorService(conf);
        MonitorExecutorService monitorExecutorService = monitorExecutorServiceModule.getExecutorService();
        when(configuration.getExecutorService()).thenReturn(monitorExecutorService);
        MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
        TasksExecutionServiceProvider tasksExecutionServiceProvider =  new TasksExecutionServiceProvider(aBaseMonitor, metricWriteHelper);
        //SampleRunnable sampleRunnable = new SampleRunnable();
        tasksExecutionServiceProvider.submit("Task1", new SampleRunnable());
        tasksExecutionServiceProvider.submit("Task2", new SampleRunnable());
        Thread.sleep(1000);
        verify(metricWriteHelper, times(0)).onComplete();
        verify(aBaseMonitor, times(0)).onComplete();
    }

}
