/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.modules.FileWatchListenerModule;
import com.appdynamics.extensions.file.FileWatchListener;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/**
 * Created by venkata.konala on 3/29/18.
 */
public class MonitorContextConfiguration {


    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MonitorContextConfiguration.class);
    private File installDir;
    private AMonitorJob aMonitorJob;
    private Map<String, ?> configYml;
    private Object metricXml;
    private String metricPrefix;
    private String defaultMetricPrefix;
    private JAXBContext jaxbContext;
    private FileWatchListenerModule fileWatchListenerModule;
    private boolean enabled;
    private MonitorContext context;

    public MonitorContextConfiguration(String monitorName, String defaultMetricPrefix, File installDir, AMonitorJob aMonitorJob) {
        this.defaultMetricPrefix = defaultMetricPrefix;
        this.installDir = installDir;
        this.aMonitorJob = aMonitorJob;
        this.fileWatchListenerModule = new FileWatchListenerModule();
        this.context = new MonitorContext(monitorName);
    }

    public void setConfigYml(String path) {
        File configFile = resolvePath(path, installDir);
        logger.info("Loading the contextConfiguration from {}", configFile.getAbsolutePath());
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(configFile);
        if (rootElem == null) {
            logger.error("Unable to get data from the config file");
            return;
        }
        configYml = rootElem;
        Boolean enabled = (Boolean) configYml.get("enabled");
        if (!Boolean.FALSE.equals(enabled)) {
            this.enabled = true;
            setMetricPrefix((String) configYml.get("metricPrefix"), defaultMetricPrefix);
        } else {
            this.enabled = false;
            logger.error("The contextConfiguration is not enabled {}", configYml);
        }
        context.initialize(aMonitorJob, getConfigYml(), getMetricPrefix());
    }


    public Map<String, ?> getConfigYml() {
        if (configYml != null) {
            return configYml;
        } else {
            throw new RuntimeException("The config is not read, please check the logs for errors");
        }
    }

    public <T> void setMetricXml(String path, final Class<T> clazz) {
        File metricFile = resolvePath(path, installDir);
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(clazz);
            } catch (JAXBException e) {
                throw new RuntimeException("Exception while initializing the jaxb context", e);
            }
        }
        try {
            logger.debug("Attempting to unmarshall the file {} into {}", metricFile.getAbsolutePath(), clazz.getName());
            FileInputStream inputStream = new FileInputStream(metricFile);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            metricXml = (T) unmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Exception while unmarshalling the file " + metricFile.getAbsolutePath(), e);
        }
    }

    public Object getMetricsXml() {
        if (metricXml != null) {
            return metricXml;
        } else {
            throw new RuntimeException("The metrics xml was not read, possibly due to error. Please check previous logs");
        }
    }

    private void setMetricPrefix(String metricPrefix, String defaultMetricPrefix) {
        logger.debug("The metric prefix from the config file is {}", metricPrefix);
        if (!Strings.isNullOrEmpty(metricPrefix)) {
            this.metricPrefix = StringUtils.trim(metricPrefix.trim(), "|");
        } else {
            this.metricPrefix = defaultMetricPrefix;
        }
        logger.info("The metric prefix is initialized as {}", this.metricPrefix);
        if (Strings.isNullOrEmpty(this.metricPrefix)) {
            throw new IllegalArgumentException("The metricPrefix cannot be resolved. Please set it in the contextConfiguration");
        }
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    private File resolvePath(String path, File installDir) {
        File file = PathResolver.getFile(path, installDir);
        if (file != null && file.exists()) {
            return file;
        } else {
            throw new IllegalArgumentException("The path [" + path + "] cannot be resolved to a file");
        }
    }

    public void registerListener(String path, FileWatchListener callback) {
        fileWatchListenerModule.createListener(path, callback, installDir, 3000);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public MonitorContext getContext() {
        return context;
    }

}
