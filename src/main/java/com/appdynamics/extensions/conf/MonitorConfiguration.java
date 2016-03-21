package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.StringUtils;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.PerMinValueCalculator;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.YmlUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by abey.tom on 3/14/16.
 * <p/>
 * A file which loads all the configuration
 */
public class MonitorConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(MonitorConfiguration.class);
    public static final String EXTENSION_WORKBENCH_MODE = "extension.workbench.mode";

    public enum ConfItem {
        CONFIG_YML, HTTP_CLIENT, METRICS_XML, METRIC_PREFIX, EXECUTOR_SERVICE, METRIC_WRITE_HELPER
    }

    public Set<File> monitoredDirs;
    private Map<File, FileWatchListener> listenerMap;
    private String defaultMetricPrefix;
    private final File installDir;
    private FileAlterationMonitor monitor;
    private Map<String, ?> config;
    private String metricPrefix;
    private ExecutorService executorService;
    private int executorServiceSize;
    private CloseableHttpClient httpClient;
    private JAXBContext jaxbContext;
    private Object metricXml;
    private PerMinValueCalculator perMinValueCalculator;
    private MetricWriteHelper metricWriter;

    public MonitorConfiguration(String defaultMetricPrefix) {
        AssertUtils.assertNotNull(defaultMetricPrefix, "The Defaut Metric Prefix cannot be empty");
        this.defaultMetricPrefix = StringUtils.trim(defaultMetricPrefix.trim(), "|");
        installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
        if (installDir == null) {
            throw new RuntimeException("The install directory cannot be located.");
        }
    }

    public CloseableHttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        } else {
            throw new RuntimeException("Cannot Initialize HttpClient.The [servers] section is not set in the config.yml");
        }
    }

    public Map<String, ?> getConfig() {
        if (config != null) {
            return config;
        } else {
            throw new RuntimeException("The config is not read, please check the logs for errors");
        }
    }

    public ExecutorService getExecutorService() {
        if (executorService != null) {
            return executorService;
        } else {
            throw new RuntimeException("The executor service is not initialized. Please make sure that the params are set in config.yml");
        }
    }

    public Object getMetricsXmlConfiguration() {
        if (metricXml != null) {
            return metricXml;
        } else {
            throw new RuntimeException("The metrics xml was not read, possibly due to error. Please check previous logs");
        }
    }

    public PerMinValueCalculator getPerMinValueCalculator() {
        if (perMinValueCalculator == null) {
            perMinValueCalculator = new PerMinValueCalculator();
        }
        return perMinValueCalculator;
    }

    public String getMetricPrefix() {
        if (!Strings.isNullOrEmpty(metricPrefix)) {
            return metricPrefix;
        } else {
            throw new RuntimeException("The metricPrefix is not resolved. Please make sure that the metricPrefix is set in the configuration");
        }
    }

    public void setConfigYml(String path) {
        FileWatchListener fileWatchListener = new FileWatchListener() {
            public void onFileChange(File file) {
                loadConfigYml(file);
            }
        };
        createListener(path, fileWatchListener);
    }

    private void createListener(String path, FileWatchListener fileWatchListener) {
        File file = resolvePath(path);
        logger.debug("The path [{}] is resolved to file {}", path, file.getAbsolutePath());
        createListener(file, fileWatchListener);
        createWatcher(file);
        //Initialize it for the fisrt time
        fileWatchListener.onFileChange(file);
    }

    public <T> void setMetricsXml(String path, final Class<T> clazz) {
        FileWatchListener fileWatchListener = new FileWatchListener() {
            public void onFileChange(File file) {
                loadMetricsXml(file, clazz);
            }
        };
        createListener(path, fileWatchListener);
    }

    private <T> void loadMetricsXml(File file, Class<T> clazz) {
        metricXml = reloadMetricXml(file, clazz);
        logger.info("Reloaded the metrics xml [{}] successfully from {} {}", metricXml, file.getAbsolutePath());
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

    private void loadConfigYml(File file) {
        logger.info("Loading the configuration from {}", file.getAbsolutePath());
        config = YmlReader.readFromFileAsMap(file);
        if (config != null) {
            metricPrefix = getMetricPrefix((String) config.get("metricPrefix"), defaultMetricPrefix);
            initExecutorService(config);
            initHttpClient(config);
        }
    }

    private void initHttpClient(Map<String, ?> config) {
        initShutdown(httpClient, 2000 * 60);
        List servers = (List) config.get("servers");
        if (servers != null && !servers.isEmpty()) {
            httpClient = Http4ClientBuilder.getBuilder(config).build();
        } else {
            logger.info("The httpClient is not initialized since the [servers] are not present in config.yml");
        }
    }

    private void initShutdown(final CloseableHttpClient oldHttpClient, final long wait) {
        if (oldHttpClient != null) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(wait);
                        logger.debug("Shutting down the old http client {}", httpClient);
                        oldHttpClient.close();
                    } catch (Exception e) {
                        logger.error("Exception while shutting down the http client" + oldHttpClient, e);
                    }
                }
            }, "HttpClient-Shutdown-Task").start();
        }
    }

    private void initExecutorService(Map<String, ?> config) {
        Integer numberOfThreads = YmlUtils.getInteger(config.get("numberOfThreads"));
        if (numberOfThreads != null) {
            if (executorService == null) {
                executorService = createThreadPool(numberOfThreads);
            } else if (numberOfThreads != executorServiceSize) {
                logger.info("The ThreadPool size has been updated from {} -> {}", executorServiceSize, numberOfThreads);
                executorService.shutdown();
                executorService = createThreadPool(numberOfThreads);
            }
            executorServiceSize = numberOfThreads;
        } else {
            logger.info("Not initializing the thread pools since the [numberOfThreads] is not set");
            executorServiceSize = 0;
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    private ExecutorService createThreadPool(Integer numberOfThreads) {
        if (numberOfThreads != null && numberOfThreads > 0) {
            logger.info("Initializing the ThreadPool with size {}", numberOfThreads);
            return Executors.newFixedThreadPool(numberOfThreads.intValue(), new ThreadFactory() {
                private int count;

                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "Monitor-Task-Thread" + (++count));
                    thread.setContextClassLoader(AManagedMonitor.class.getClassLoader());
                    return thread;
                }
            });
        } else {
            return null;
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

    private void createListener(File file, FileWatchListener fileWatchListener) {
        if (listenerMap == null) {
            listenerMap = new HashMap<File, FileWatchListener>();
        }
        listenerMap.put(file, fileWatchListener);
    }

    private void createWatcher(File file) {
        File dir = file.getParentFile();
        if (monitor == null) {
            initMonitor();
        }
        if (monitoredDirs == null || !monitoredDirs.contains(dir)) {
            logger.debug("Creating a watcher for the directory {}", dir.getAbsolutePath());
            FileAlterationObserver observer = new FileAlterationObserver(dir);
            observer.addListener(new FileAlterationListenerAdaptor() {
                @Override
                public void onFileChange(File file) {
                    try {
                        logger.info("The file {} has been modified", file.getAbsolutePath());
                        FileWatchListener fileWatchListener = listenerMap.get(file);
                        if (fileWatchListener != null) {
                            fileWatchListener.onFileChange(file);
                        }
                    } catch (Exception e) {
                        logger.error("Error while invoking the file watch listener", e);
                    } finally {
                        if (metricWriter != null) {
                            metricWriter.reset();
                        }
                    }
                }
            });
            monitor.addObserver(observer);
            if (monitoredDirs == null) {
                monitoredDirs = new HashSet<File>();
            }
            monitoredDirs.add(dir);
        }
    }

    private void initMonitor() {
        int interval;
        if (isWorkbenchMode()) {
            interval = 3000;
        } else {
            interval = 30000;
        }
        logger.debug("Created a FileAlterationMonitor with an interval of {}",interval);
        monitor = new FileAlterationMonitor(interval);
        try {
            monitor.start();
        } catch (Exception e) {
            logger.error("Exception while starting the FileAlterationMonitor", e);
        }
    }

    private File resolvePath(String path) {
        File file = PathResolver.getFile(path, installDir);
        if (file != null && file.exists()) {
            return file;
        } else {
            throw new IllegalArgumentException("The path [" + path + "] cannot be resolved to a file");
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
                        AssertUtils.assertNotNull(executorService, "The executor service is not loaded. Make sure that the pool configuration in config.yml is correct");
                        break;
                    case HTTP_CLIENT:
                        AssertUtils.assertNotNull(httpClient, "The HttpClient is null. Make sure that the [servers] element in the config.yml is present");
                        break;
                    case METRIC_PREFIX:
                        AssertUtils.assertNotNull(metricPrefix, "The metricPrefix is not set in config.yml");
                        break;
                    case METRICS_XML:
                        AssertUtils.assertNotNull(metricXml, "The metrics path is incorect or it contains errors. Please check the logs");
                        break;
                    case METRIC_WRITE_HELPER:
                        AssertUtils.assertNotNull(metricWriter, "The metric write helper is not set.");
                        break;

                }

            }
        }
    }

    public MetricWriteHelper getMetricWriter() {
        return metricWriter;
    }

    public void setMetricWriter(MetricWriteHelper metricWriter) {
        this.metricWriter = metricWriter;
    }

    public static boolean isWorkbenchMode() {
        return "true".equals(System.getProperty(MonitorConfiguration.EXTENSION_WORKBENCH_MODE));
    }

}
