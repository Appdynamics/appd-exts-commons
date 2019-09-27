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
import com.appdynamics.extensions.conf.processor.ConfigProcessor;
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
    private Map<String, ?> configYml;
    private Object metricXml;
    private String metricPrefix;
    private String defaultMetricPrefix;
    private JAXBContext jaxbContext;
    private boolean enabled;
    private MonitorContext context;

    public MonitorContextConfiguration(String monitorName, String defaultMetricPrefix, File installDir) {
        this.defaultMetricPrefix = defaultMetricPrefix;
        this.installDir = installDir;
        this.context = new MonitorContext(monitorName, installDir);
    }

    public void loadConfigYml(String path) {
        File configFile = resolvePath(path, installDir);
        logger.info("Loading the contextConfiguration from {}", configFile.getAbsolutePath());
        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(configFile);
        if (rootElem == null) {
            logger.error("Unable to get data from the config file");
            return;
        }
        rootElem =ConfigProcessor.process(rootElem);
        configYml = rootElem;
        Boolean enabled = (Boolean) configYml.get("enabled");
        if (!Boolean.FALSE.equals(enabled)) {
            this.enabled = true;
            setMetricPrefix((String) configYml.get("metricPrefix"), defaultMetricPrefix);
        } else {
            this.enabled = false;
            logger.error("The contextConfiguration is not enabled {}", configYml);
        }
    }


    public Map<String, ?> getConfigYml() {
        if (configYml != null) {
            return configYml;
        } else {
            throw new RuntimeException("The config is not read, please check the logs for errors");
        }
    }

    public <T> void loadMetricXml(String path, final Class<T> clazz) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public MonitorContext getContext() {
        return context;
    }

    public void initialize(AMonitorJob monitorJob, boolean isConfigYmlReloaded){
        getContext().initialize(monitorJob,getConfigYml(),getMetricPrefix(),isConfigYmlReloaded);
    }

}
