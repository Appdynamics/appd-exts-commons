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

package com.appdynamics.extensions.conf.monitorxml;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * Created by abey.tom on 3/16/16.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Monitor {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(Monitor.class);
    @XmlElement(name = "monitor-run-task")
    private MonitorRunTask monitorRunTask;

    public static Monitor from(File dir) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Monitor.class);
            if (dir != null && dir.exists()) {
                File file = new File(dir, "monitor.xml");
                if (file.exists()) {
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    Monitor monitor = (Monitor) unmarshaller.unmarshal(file);
                    if (!(monitor != null
                            && monitor.getMonitorRunTask() != null
                            && monitor.getMonitorRunTask().getJavaTask() != null)) {
                        logger.error("The monitor.xml file is not valid " + file.getAbsolutePath());
                    } else {
                        return monitor;
                    }
                } else {
                    logger.error("Cannot find the monitior file at {}", file.getAbsolutePath());
                }
            } else {
                logger.error("Cannot resolve the directory", dir != null ? dir.getAbsolutePath() : null);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    public MonitorRunTask getMonitorRunTask() {
        return monitorRunTask;
    }

    public void setMonitorRunTask(MonitorRunTask monitorRunTask) {
        this.monitorRunTask = monitorRunTask;
    }
}
