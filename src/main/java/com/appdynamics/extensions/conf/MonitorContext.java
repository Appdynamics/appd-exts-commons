/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.conf.modules.*;
import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.appdynamics.extensions.metrics.PerMinValueCalculator;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by venkata.konala on 3/29/18.
 */
public class MonitorContext {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MonitorContext.class);
    public static final String EXTENSION_WORKBENCH_MODE = "extension.workbench.mode";
    private String monitorName;
    private Map<String, ?> config;
    private String metricPrefix;

    private WorkBenchModule workBenchModule;
    private HttpClientModule httpClientModule;
    private MonitorExecutorServiceModule monitorExecutorServiceModule;
    private JobScheduleModule jobScheduleModule;
    private CacheModule cacheModule;
    private DerivedMetricsModule derivedMetricsModule;
    private PerMinValueCalculatorModule perMinValueCalculatorModule;
    private HealthCheckModule healthCheckModule;
    private MetricCharSequenceReplaceModule metricCharSequenceReplaceModule;
    private EventsServiceModule eventsServiceModule;

    MonitorContext(String monitorName) {
        this.monitorName = monitorName;
        workBenchModule = new WorkBenchModule();
        httpClientModule = new HttpClientModule();
        monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        jobScheduleModule = new JobScheduleModule();
        cacheModule = new CacheModule();
        derivedMetricsModule = new DerivedMetricsModule();
        perMinValueCalculatorModule = new PerMinValueCalculatorModule();
        healthCheckModule = new HealthCheckModule();
        metricCharSequenceReplaceModule = new MetricCharSequenceReplaceModule();
        eventsServiceModule = new EventsServiceModule();
    }

    public void initialize(AMonitorJob monitorJob, Map<String, ?> config, String metricPrefix) {
        this.config = config;
        this.metricPrefix = metricPrefix;
        Boolean enabled = (Boolean) config.get("enabled");
        if (!Boolean.FALSE.equals(enabled)) {
            workBenchModule.initWorkBenchStore(config, metricPrefix);
            httpClientModule.initHttpClient(config);
            monitorExecutorServiceModule.initExecutorService(config, monitorName);
            jobScheduleModule.initScheduledJob(config, monitorName, monitorJob);
            cacheModule.initCache();
            healthCheckModule.initMATroubleshootChecks(monitorName, config);
            metricCharSequenceReplaceModule.initMetricCharSequenceReplacer(config);
            logger.info("Charset is {}, file encoding is {}", Charset.defaultCharset(), System.getProperty("file.encoding"));
            eventsServiceModule.initEventsServiceDataManager(monitorName, config);
        } else {
            logger.error("The contextConfiguration is not enabled {}", config);
        }
    }

    public static boolean isWorkbenchMode() {
        return "true".equals(System.getProperty(EXTENSION_WORKBENCH_MODE));
    }

    public WorkBenchModule getWorkBenchModule() {
        return workBenchModule;
    }

    public void setWorkBenchModule(WorkBenchModule workBenchModule) {
        this.workBenchModule = workBenchModule;
    }

    public void setHttpClientModule(HttpClientModule httpClientModule) {
        this.httpClientModule = httpClientModule;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClientModule.getHttpClient();
    }

    public void setMonitorExecutorServiceModule(MonitorExecutorServiceModule monitorExecutorServiceModule) {
        this.monitorExecutorServiceModule = monitorExecutorServiceModule;
    }

    public MonitorExecutorService getExecutorService() {
        return monitorExecutorServiceModule.getExecutorService();
    }

    public JobScheduleModule getJobScheduleModule() {
        return jobScheduleModule;
    }

    public void setJobScheduleModule(JobScheduleModule jobScheduleModule) {
        this.jobScheduleModule = jobScheduleModule;
    }

    public void setCacheModule(CacheModule cacheModule) {
        this.cacheModule = cacheModule;
    }

    public boolean isScheduledModeEnabled() {
        return jobScheduleModule.getScheduler() != null;
    }

    public ConcurrentMap<String, Metric> getCachedMetrics() {
        return cacheModule.getMetricCache().asMap();
    }

    public void putInMetricCache(String metricPath, Metric metric) {
        cacheModule.putInMetricCache(metricPath, metric);
    }

    public MetricWriter getFromWriterCache(String metricPath) {
        return cacheModule.getWriterCache().getIfPresent(metricPath);
    }

    public void putInWriterCache(String metricPath, MetricWriter writer) {
        cacheModule.putInWriterCache(metricPath, writer);
    }

    public DerivedMetricsCalculator createDerivedMetricsCalculator() {
        return derivedMetricsModule.initDerivedMetricsCalculator(config, metricPrefix);
    }

    public PerMinValueCalculator getPerMinValueCalculator() {
        return perMinValueCalculatorModule.getPerMinValueCalculator();
    }

    public MetricCharSequenceReplacer getMetricCharSequenceReplacer() {
        return metricCharSequenceReplaceModule.getMetricCharSequenceReplacer();
    }

    public void setMetricCharSequenceReplaceModule (MetricCharSequenceReplaceModule metricCharSequenceReplaceModule) {
        this.metricCharSequenceReplaceModule = metricCharSequenceReplaceModule;
    }

    public EventsServiceDataManager getEventsServiceDataManager() {
        return eventsServiceModule.getEventsServiceDataManager();
    }

    public void setEventsServiceModule(EventsServiceModule eventsServiceModule) {
        this.eventsServiceModule = eventsServiceModule;
    }
}
