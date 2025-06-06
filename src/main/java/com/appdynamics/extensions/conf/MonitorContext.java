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

package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.SystemPropertyConstants;
import com.appdynamics.extensions.conf.modules.*;
import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.appdynamics.extensions.metrics.PerMinValueCalculator;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
/**
 * Created by venkata.konala on 3/29/18.
 */
public class MonitorContext {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MonitorContext.class);
    private String monitorName;
    private Map<String, ?> config;
    private String metricPrefix;
    private File installDir;
    private ControllerModule controllerModule;
    private WorkBenchModule workBenchModule;
    private HttpClientModule httpClientModule;
    private MonitorExecutorServiceModule monitorExecutorServiceModule;
    private JobScheduleModule jobScheduleModule;
    private CacheModule cacheModule;
    private DerivedMetricsModule derivedMetricsModule;
    private PerMinValueCalculatorModule perMinValueCalculatorModule;
    private HealthCheckModule healthCheckModule;
    private CustomDashboardModule dashboardModule;
    private MetricCharSequenceReplaceModule metricCharSequenceReplaceModule;
    private EventsServiceModule eventsServiceModule;

    MonitorContext(String monitorName, File installDir) {
        this.installDir = installDir;
        this.monitorName = monitorName;
        controllerModule = new ControllerModule();
        workBenchModule = new WorkBenchModule();
        httpClientModule = new HttpClientModule();
        monitorExecutorServiceModule = new MonitorExecutorServiceModule();
        jobScheduleModule = new JobScheduleModule();
        cacheModule = new CacheModule();
        derivedMetricsModule = new DerivedMetricsModule();
        perMinValueCalculatorModule = new PerMinValueCalculatorModule();
        metricCharSequenceReplaceModule = new MetricCharSequenceReplaceModule();
        eventsServiceModule = new EventsServiceModule();
        healthCheckModule = new HealthCheckModule();
        dashboardModule = new CustomDashboardModule();
    }

    public void initialize(AMonitorJob monitorJob, Map<String, ?> config, String metricPrefix) {
        this.config = config;
        this.metricPrefix = metricPrefix;
        Boolean enabled = (Boolean) config.get("enabled");
        if (!Boolean.FALSE.equals(enabled)) {
            logger.info("Charset is {}, file encoding is {}", Charset.defaultCharset(), System.getProperty("file.encoding"));
            controllerModule.initController(installDir, config);
            workBenchModule.initWorkBenchStore(config, metricPrefix, getControllerInfo());
            httpClientModule.initHttpClient(config);
            monitorExecutorServiceModule.initExecutorService(config, monitorName);
            jobScheduleModule.initScheduledJob(config, monitorName, monitorJob);
            cacheModule.initCache();
            metricCharSequenceReplaceModule.initMetricCharSequenceReplacer(config);
            eventsServiceModule.initEventsServiceDataManager(monitorName, config);
            healthCheckModule.initMATroubleshootChecks(config, monitorName, metricPrefix, getControllerInfo(), getControllerAPIService());
            dashboardModule.initCustomDashboard(config, metricPrefix, monitorName, getControllerInfo(), getControllerAPIService());
        } else {
            logger.error("The contextConfiguration is not enabled {}", config);
        }
    }

    public void setControllerModule(ControllerModule controllerModule) {
        this.controllerModule = controllerModule;
    }

    public ControllerInfo getControllerInfo() {
        return controllerModule.getControllerInfo();
    }

    public ControllerClient getControllerClient() {
        return controllerModule.getControllerClient();
    }

    public ControllerAPIService getControllerAPIService() {
        return controllerModule.getControllerAPIService();
    }

    public void setWorkBenchModule(WorkBenchModule workBenchModule) {
        this.workBenchModule = workBenchModule;
    }

    public static boolean isWorkbenchMode() {
        return "true".equals(System.getProperty(SystemPropertyConstants.WORKBENCH_MODE_PROPERTY));
    }

    public WorkBenchModule getWorkBenchModule() {
        return workBenchModule;
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

    public void setJobScheduleModule(JobScheduleModule jobScheduleModule) {
        this.jobScheduleModule = jobScheduleModule;
    }

    public boolean isScheduledModeEnabled() {
        return jobScheduleModule.getScheduler() != null;
    }

    public JobScheduleModule getJobScheduleModule() {
        return jobScheduleModule;
    }

    public void setCacheModule(CacheModule cacheModule) {
        this.cacheModule = cacheModule;
    }

    public ConcurrentMap<String, Metric> getCachedMetrics() {
        return cacheModule.getMetricCache().asMap();
    }

    public void putInMetricCache(String metricPath, Metric metric) {
        cacheModule.putInMetricCache(metricPath, metric);
    }

    public void putInWriterCache(String metricPath, MetricWriter writer) {
        cacheModule.putInWriterCache(metricPath, writer);
    }

    public MetricWriter getFromWriterCache(String metricPath) {
        return cacheModule.getWriterCache().getIfPresent(metricPath);
    }

    public DerivedMetricsCalculator createDerivedMetricsCalculator() {
        return derivedMetricsModule.initDerivedMetricsCalculator(config, metricPrefix);
    }

    public PerMinValueCalculator getPerMinValueCalculator() {
        return perMinValueCalculatorModule.getPerMinValueCalculator();
    }

    public void setMetricCharSequenceReplaceModule (MetricCharSequenceReplaceModule metricCharSequenceReplaceModule) {
        this.metricCharSequenceReplaceModule = metricCharSequenceReplaceModule;
    }

    public MetricCharSequenceReplacer getMetricCharSequenceReplacer() {
        return metricCharSequenceReplaceModule.getMetricCharSequenceReplacer();
    }

    public void setEventsServiceModule(EventsServiceModule eventsServiceModule) {
        this.eventsServiceModule = eventsServiceModule;
    }

    public EventsServiceDataManager getEventsServiceDataManager() {
        return eventsServiceModule.getEventsServiceDataManager();
    }

    public void setDashboardModule(CustomDashboardModule dashboardModule) {
        this.dashboardModule = dashboardModule;
    }

    public CustomDashboardModule getDashboardModule() {
        return dashboardModule;
    }
}
