/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.conf.modules.DerivedMetricsModule;
import com.appdynamics.extensions.conf.modules.FileWatchListenerModule;
import com.appdynamics.extensions.conf.modules.WorkBenchModule;
import com.appdynamics.extensions.conf.monitorxml.Monitor;
import com.appdynamics.extensions.file.FileWatchListener;
import com.appdynamics.extensions.metrics.derived.DerivedMetricsCalculator;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/**
 * Created by venkata.konala on 3/29/18.
 */
public class ExtensionConfiguration {


    public static final Logger logger = LoggerFactory.getLogger(ExtensionConfiguration.class);
    private File installDir;
    private Map<String, ?> configYml;
    private Object metricXml;
    private String metricPrefix;
    private String defaultMetricPrefix;
    private JAXBContext jaxbContext;
    private FileWatchListenerModule fileWatchListenerModule;
    private boolean enabled;

    public ExtensionConfiguration(String defaultMetricPrefix, File installDir){
        this.defaultMetricPrefix = defaultMetricPrefix;
        this.installDir = installDir;
        if(installDir == null){
            throw new RuntimeException("The install directory cannot be null");
        }
        fileWatchListenerModule = new FileWatchListenerModule();
    }

    public void setConfigYml(String path){
        File configFile = resolvePath(path, installDir);
        logger.info("Loading the configuration from {}", configFile.getAbsolutePath());
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(configFile);
        if(rootElem == null){
            logger.error("The element [{}] was not found in the config file", rootElem);
            return;
        }
        configYml = rootElem;
        Boolean enabled = (Boolean) configYml.get("enabled");
        if(!Boolean.FALSE.equals(enabled)) {
            this.enabled = true;
            setMetricPrefix((String) configYml.get("metricPrefix"), defaultMetricPrefix);
        }
        else{
            this.enabled = false;
            logger.error("The configuration is not enabled {}", configYml);
        }
    }


    public Map<String, ?> getConfigYml() {
        if (configYml != null) {
            return configYml;
        } else {
            throw new RuntimeException("The config is not read, please check the logs for errors");
        }
    }

    public <T> void setMetricXml(String path, final Class<T> clazz){
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

    private void setMetricPrefix(String metricPrefix, String defaultMetricPrefix){
        logger.debug("The metric prefix from the config file is {}", metricPrefix);
        if (!Strings.isNullOrEmpty(metricPrefix)) {
            this.metricPrefix = StringUtils.trim(metricPrefix.trim(), "|");
        } else {
            this.metricPrefix = defaultMetricPrefix;
        }
        logger.info("The metric prefix is initialized as {}", metricPrefix);
        if (Strings.isNullOrEmpty(metricPrefix)) {
            throw new IllegalArgumentException("The metricPrefix cannot be resolved. Please set it in the configuration");
        }
    }

    public String getMetricPrefix(){
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

    public void registerListener(String path, FileWatchListener callback, ExtensionContext context){
        fileWatchListenerModule.createListener(path , callback, installDir, context.getWorkBenchModule().getWorkBench(), 3000);
    }

    public boolean isEnabled(){
        return enabled;
    }

}
