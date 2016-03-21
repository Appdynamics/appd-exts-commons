package com.appdynamics.extensions.conf.monitorxml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final Logger logger = LoggerFactory.getLogger(Monitor.class);
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
