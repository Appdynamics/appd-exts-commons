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
import com.appdynamics.extensions.conf.modules.DerivedMetricsModule;
import com.appdynamics.extensions.conf.modules.MonitorExecutorServiceModule;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        when(configuration.getContext()).thenReturn(context);
        when(context.createDerivedMetricsCalculator()).thenReturn(null);
        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);
        when(aBaseMonitor.getContextConfiguration().getMetricPrefix()).thenReturn("Custom Metrics|Sample Monitor|");
        List<Metric> metricList = Lists.newArrayList();
        Metric metric1 = new Metric("sample", "2.04", "Custom Metrics|Sample Monitor|sample");
        metricList.add(metric1);

        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        metricWriteHelper.transformAndPrintMetrics(metricList);
        verify(metricWriter, times(1)).printMetric(stringArgumentCaptor.capture());

        List<String> stringArgs = stringArgumentCaptor.getAllValues();
        Assert.assertTrue(stringArgs.get(0).equals("2"));
    }

    @Test
    public void transformAndPrintMetricWithDerivedTest(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        when(aBaseMonitor.getContextConfiguration().getMetricPrefix()).thenReturn("Custom Metrics|Sample Monitor|");
        DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();

        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/DerivedSample.yml"));
        DerivedMetricsCalculator derivedMetricsCalculator = derivedMetricsModule.initDerivedMetricsCalculator(conf, "Custom Metrics|Sample Monitor");
        when(configuration.getContext()).thenReturn(context);
        when(context.createDerivedMetricsCalculator()).thenReturn(derivedMetricsCalculator);
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
        verify(metricWriter, times(5)).printMetric(stringArgumentCaptor.capture());
        List<String> stringArgs = stringArgumentCaptor.getAllValues();
        for(String value : stringArgs){
            Assert.assertTrue(value.equals("2") || value.equals("4") || value.equals("1") || value.equals("5"));
        }

    }

    @Test
    public void printMetricWithMetricTypeDoesRoundValueTest(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        when(configuration.getContext()).thenReturn(context);
        when(context.createDerivedMetricsCalculator()).thenReturn(null);
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
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        MonitorContext context = mock(MonitorContext.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        when(configuration.getContext()).thenReturn(context);
        when(context.createDerivedMetricsCalculator()).thenReturn(null);
        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);


        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        metricWriteHelper.printMetric("Custom Metrics|Sample Monitor|sample1", null,MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
        MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
        verify(metricWriter, times(0)).printMetric(stringArgumentCaptor.capture());
    }

    @Test
    public void whenDoneThenCheckTotalNumberOfMetrics(){
        ABaseMonitor aBaseMonitor = mock(ABaseMonitor.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        configuration.setConfigYml("src/test/resources/conf/config_WithMultipleServersDisplayNameCheck.yml");

        MonitorContext context = mock(MonitorContext.class);
        when(aBaseMonitor.getContextConfiguration()).thenReturn(configuration);
        when(aBaseMonitor.getContextConfiguration().getMetricPrefix()).thenReturn("Custom Metrics|Sample Monitor|");
        when(configuration.getContext()).thenReturn(context);

        MetricWriteHelper metricWriteHelper = new MetricWriteHelper(aBaseMonitor);
        List<Metric> metricList = Lists.newArrayList();
        Metric metric1 = new Metric("sample1", "10", "Custom Metrics|Sample Monitor|sample1");
        Metric metric2 = new Metric("sample2", "20", "Custom Metrics|Sample Monitor|sample");
        Metric metric3 = new Metric("sample3", "30", "Custom Metrics|Sample Monitor|sample");
        metricList.add(metric1);
        metricList.add(metric2);
        metricList.add(metric3);
        MetricWriter metricWriter = mock(MetricWriter.class);
        when(aBaseMonitor.getMetricWriter(anyString(), anyString(), anyString(), anyString())).thenReturn(metricWriter);
        metricWriteHelper.transformAndPrintMetrics(metricList);
        metricWriteHelper.onComplete();

        verify(metricWriter, times(4)).printMetric(argumentCaptor.capture());
        List<String> metrics = argumentCaptor.getAllValues();
        Assert.assertTrue("Total number of metrics reported is not correct", Integer.valueOf(metrics.get(3) ) == 3);


    }
}

