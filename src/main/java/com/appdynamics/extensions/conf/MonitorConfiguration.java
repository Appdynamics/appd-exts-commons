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
 *//*


package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.conf.modules.*;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.PerMinValueCalculator;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

*/
/**
 * Created by abey.tom on 3/14/16.
 * <p/>
 * A file which loads all the configuration
 * <p/>
 * TODO checkIfInitialized should be called after each reload of configuration
 * TODO have the default-yml in the codebase and merge it with the other YML
 *//*

public class MonitorConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(MonitorConfiguration.class);
    public static final String EXTENSION_WORKBENCH_MODE = "extension.workbench.mode";

    private String monitorName;
    private AMonitorJob aMonitorJob;
    public enum ConfItem {
        CONFIG_YML, HTTP_CLIENT, METRICS_XML, METRIC_PREFIX, EXECUTOR_SERVICE
    }
    private String defaultMetricPrefix;
    private final File installDir;
    private Map<String, ?> config;
    private String metricPrefix;
    private JAXBContext jaxbContext;
    private Object metricXml;
    private boolean enabled;

    private HttpClientModule httpClientModule = new HttpClientModule();
    private MonitorExecutorServiceModule monitorExecutorServiceModule = new MonitorExecutorServiceModule();
    private WorkBenchModule workBenchModule = new WorkBenchModule();
    private DerivedMetricsModule derivedMetricsModule = new DerivedMetricsModule();
    private FileWatchListenerModule fileWatchListenerModule = new FileWatchListenerModule();
    private PerMinValueCalculatorModule perMinValueCalculatorModule = new PerMinValueCalculatorModule();
    private JobScheduleModule jobScheduleModule = new JobScheduleModule();
    private CacheModule cacheModule = new CacheModule();

    public MonitorConfiguration(String monitorName, String defaultMetricPrefix, AMonitorJob aMonitorJob) {
        AssertUtils.assertNotNull(monitorName,"The monitor name cannot be empty");
        AssertUtils.assertNotNull(defaultMetricPrefix, "The Default Metric Prefix cannot be empty");
        AssertUtils.assertNotNull(aMonitorJob, "The Runnable[aMonitorJob] cannot be null");
        this.monitorName = monitorName;
        this.defaultMetricPrefix = StringUtils.trim(defaultMetricPrefix.trim(), "|");
        this.aMonitorJob = aMonitorJob;
        installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
        if (installDir == null) {
            throw new RuntimeException("The install directory cannot be located.");
        }
    }

    public Map<String, ?> getConfigYml() {
        if (config != null) {
            return config;
        } else {
            throw new RuntimeException("The config is not read, please check the logs for errors");
        }
    }

    public Object getMetricsXmlConfiguration() {
        if (metricXml != null) {
            return metricXml;
        } else {
            throw new RuntimeException("The metrics xml was not read, possibly due to error. Please check previous logs");
        }
    }

    public String getMetricPrefix() {
        if (!Strings.isNullOrEmpty(metricPrefix)) {
            return metricPrefix;
        } else {
            return defaultMetricPrefix;
        }
    }

    public void setConfigYml(String path, final FileWatchListener callback) {
        setConfigYml(path, callback, null);
    }

    public void setConfigYml(String path, final FileWatchListener callback, final String rootElement) {
        FileWatchListener fileWatchListener = new FileWatchListener() {
            public void onFileChange(File file) {
                loadConfigYml(file, rootElement);
                if (callback != null) {
                    callback.onFileChange(file);
                }
            }
        };
        fileWatchListenerModule.createListener(path, fileWatchListener, installDir, workBenchModule.getWorkBench(), 30000);
    }

    public void setConfigYml(String path) {
        setConfigYml(path, null, null);
    }

    public void setConfigYml(String path, String rootElement) {
        setConfigYml(path, null, rootElement);
    }

    public <T> void setMetricsXml(String path, final Class<T> clazz) {
        setMetricsXml(path, clazz, null);
    }

    public <T> void setMetricsXml(String path, final Class<T> clazz, final FileWatchListener callback) {
        FileWatchListener fileWatchListener = new FileWatchListener() {
            public void onFileChange(File file) {
                loadMetricsXml(file, clazz);
                if (callback != null) {
                    callback.onFileChange(file);
                }
            }
        };
        fileWatchListenerModule.createListener(path, fileWatchListener, installDir, workBenchModule.getWorkBench(), 30000);
    }

    private <T> void loadMetricsXml(File file, Class<T> clazz) {
        metricXml = reloadMetricXml(file, clazz);
        logger.info("Reloaded the metrics xml [{}] successfully from {}", metricXml, file.getAbsolutePath());
    }

    private <T> T reloadMetricXml(File file, Class<T> clazz) {
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(clazz);
            } catch (JAXBException e) {
                throw new RuntimeException("Exception while initializing the jaxb context", e);
            }
        }
        try {
            logger.debug("Attempting to unmarshall the file {} into {}", file.getAbsolutePath(), clazz.getName());
            FileInputStream inputStream = new FileInputStream(file);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Exception while unmarshalling the file " + file.getAbsolutePath(), e);
        }
    }

    protected void loadConfigYml(File file, String rootName) {
        logger.info("Loading the configuration from {}", file.getAbsolutePath());
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(file);
        if (rootElem != null) {
            if (rootName != null) {
                config = (Map<String, ?>) rootElem.get(rootName);
                if (config == null) {
                    logger.error("The element [{}] was not found in the config file", rootName);
                    return;
                }
            } else {
                config = rootElem;
            }
            Boolean enabled = (Boolean) config.get("enabled");
            if(!Boolean.FALSE.equals(enabled)){
                this.enabled = true;
                metricPrefix = getMetricPrefix((String) config.get("metricPrefix"), defaultMetricPrefix);
                workBenchModule.initWorkBenchStore(config, metricPrefix);
                monitorExecutorServiceModule.initExecutorService(config);
                httpClientModule.initHttpClient(config);
                jobScheduleModule.initScheduledJob(config, monitorName, aMonitorJob);
                cacheModule.initCache();
            } else{
                this.enabled = false;
                logger.error("The configuration is not enabled {}", config);
            }
        }
    }

    protected static String getMetricPrefix(String metricPrefix, String defaultMetricPrefix) {
        logger.debug("The metric prefix from the config file is {}", metricPrefix);
        if (!Strings.isNullOrEmpty(metricPrefix)) {
            metricPrefix = StringUtils.trim(metricPrefix.trim(), "|");
        } else {
            metricPrefix = defaultMetricPrefix;
        }
        logger.info("The metric prefix is initialized as {}", metricPrefix);
        if (!Strings.isNullOrEmpty(metricPrefix)) {
            return metricPrefix;
        } else {
            throw new IllegalArgumentException("The metricPrefix cannot be resolved. Please set it in the configuration");
        }
    }

    public interface FileWatchListener {
        void onFileChange(File file);
    }

    public void checkIfInitialized(ConfItem... items) {
        if (items != null) {
            for (ConfItem item : items) {
                switch (item) {
                    case CONFIG_YML:
                        AssertUtils.assertNotNull(config, "The config yml is not loaded. Please check the previous logs. Make sure that the yml syntax is correct");
                        break;
                    case EXECUTOR_SERVICE:
                        AssertUtils.assertNotNull(monitorExecutorServiceModule.getExecutorService(), "The executor service is not loaded. Make sure that the pool configuration in config.yml is correct");
                        break;
                    case HTTP_CLIENT:
                        AssertUtils.assertNotNull(httpClientModule.getHttpClient(), "The HttpClient is null. Make sure that the [servers] element in the config.yml is present");
                        break;
                    case METRIC_PREFIX:
                        AssertUtils.assertNotNull(metricPrefix, "The metricPrefix is not set in config.yml");
                        break;
                    case METRICS_XML:
                        AssertUtils.assertNotNull(metricXml, "The metrics path is incorect or it contains errors. Please check the logs");
                        break;
                }

            }
        }
    }

    public CloseableHttpClient getHttpClient(){
        return httpClientModule.getHttpClient();
    }

    public PerMinValueCalculator getPerMinValueCalculator(){
        return perMinValueCalculatorModule.getPerMinValueCalculator();
    }

    public MonitorExecutorService getExecutorService(){
        return monitorExecutorServiceModule.getExecutorService();
    }

    public DerivedMetricsCalculator createDerivedMetricsCalculator(){
        return derivedMetricsModule.initDerivedMetricsCalculator(config, getMetricPrefix());
    }

    public static boolean isWorkbenchMode() {
        return "true".equals(System.getProperty(MonitorConfiguration.EXTENSION_WORKBENCH_MODE));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isScheduledModeEnabled(){
        return jobScheduleModule.getScheduler() != null;
    }

    public ConcurrentMap<String,Metric> getCachedMetrics(){
        return cacheModule.getMetricCache().asMap();
    }

    public void putInMetricCache(String metricPath, Metric metric){
        cacheModule.putInMetricCache(metricPath,metric);
    }

    public MetricWriter getFromWriterCache(String metricPath) {
        return cacheModule.getWriterCache().getIfPresent(metricPath);
    }

    public void putInWriterCache(String metricPath, MetricWriter writer) {
        cacheModule.putInWriterCache(metricPath,writer);
    }

}
*/
