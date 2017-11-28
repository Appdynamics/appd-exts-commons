package com.appdynamics.extensions;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.conf.modules.DerivedMetricsModule;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by venkata.konala on 10/31/17.
 */
public class MetricWriteHelperTest {

    @Test
    public void transformAndPrintMetricWithRoundingTest(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorConfiguration configuration = mock(MonitorConfiguration.class);
        when(aBaseMonitor.getConfiguration()).thenReturn(configuration);
        when(configuration.createDerivedMetricsCalculator()).thenReturn(null);
        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);
        List<Metric> metricList = Lists.newArrayList();
        Metric metric1 = new Metric("sample", "2.04", "Custom Metrics|Sample Monitor|sample");
        metricList.add(metric1);

        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        metricWriteHelper.transformAndPrintMetrics(metricList);
        verify(metricWriter, times(1)).printMetric(stringArgumentCaptor.capture());

        String stringArgs = stringArgumentCaptor.getValue();
        Assert.assertTrue(stringArgs.equals("2"));
    }

    @Test
    public void transformAndPrintMetricWithDerivedTest(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorConfiguration configuration = mock(MonitorConfiguration.class);
        when(aBaseMonitor.getConfiguration()).thenReturn(configuration);
        DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();

        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/DerivedSample.yml"));
        DerivedMetricsCalculator derivedMetricsCalculator = derivedMetricsModule.initDerivedMetricsCalculator(conf, "Custom Metrics|Sample Monitor");
        when(configuration.createDerivedMetricsCalculator()).thenReturn(derivedMetricsCalculator);
        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);

        List<Metric> metricList = Lists.newArrayList();
        Metric metric1 = new Metric("sample1", "2.04", "Custom Metrics|Sample Monitor|sample1");
        Metric metric2 = new Metric("sample2", "2.06", "Custom Metrics|Sample Monitor|sample2");
        metricList.add(metric1);
        metricList.add(metric2);
        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        metricWriteHelper.transformAndPrintMetrics(metricList);
        metricWriteHelper.onComplete();
        verify(metricWriter, times(4)).printMetric(stringArgumentCaptor.capture());
        List<String> stringArgs = stringArgumentCaptor.getAllValues();
        for(String value : stringArgs){
            Assert.assertTrue(value.equals("2") || value.equals("4") || value.equals("1"));
        }

    }

    @Test
    public void printMetricWithMetricTypeDoesRoundValueTest(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorConfiguration configuration = mock(MonitorConfiguration.class);
        when(aBaseMonitor.getConfiguration()).thenReturn(configuration);
        when(configuration.createDerivedMetricsCalculator()).thenReturn(null);
        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);


        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);

        metricWriteHelper.printMetric("Custom Metrics|Sample Monitor|sample1",  new BigDecimal("2"), "AVG.SUM.IND");
        metricWriteHelper.printMetric("Custom Metrics|Sample Monitor|sample2",  new BigDecimal("2.5"), "SUM.AVG.IND");
        metricWriteHelper.printMetric("Custom Metrics|Sample Monitor|sample3",  new BigDecimal("2.45"), "OBS.CUR.COL");
        verify(metricWriter, times(3)).printMetric(stringArgumentCaptor.capture());

        List<String> stringArgs = stringArgumentCaptor.getAllValues();
        for(String value : stringArgs){
            Assert.assertTrue(value.equals("2") || value.equals("3"));
        }

    }

    @Test
    public void whenMetricValuesNullShouldNotPrint(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorConfiguration configuration = mock(MonitorConfiguration.class);
        when(aBaseMonitor.getConfiguration()).thenReturn(configuration);
        when(configuration.createDerivedMetricsCalculator()).thenReturn(null);
        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);


        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        metricWriteHelper.printMetric("Custom Metrics|Sample Monitor|sample1", null,MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
        MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
        verify(metricWriter, times(0)).printMetric(stringArgumentCaptor.capture());
    }
}
