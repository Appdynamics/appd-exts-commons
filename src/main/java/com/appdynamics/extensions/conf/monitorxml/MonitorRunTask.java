package com.appdynamics.extensions.conf.monitorxml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by abey.tom on 3/16/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MonitorRunTask {
    @XmlElement(name = "task-arguments")
    private TaskArguments taskArguments;
    @XmlElement(name = "java-task")
    private JavaTask javaTask;

    public JavaTask getJavaTask() {
        return javaTask;
    }

    public void setJavaTask(JavaTask javaTask) {
        this.javaTask = javaTask;
    }

    public TaskArguments getTaskArguments() {
        return taskArguments;
    }

    public void setTaskArguments(TaskArguments taskArguments) {
        this.taskArguments = taskArguments;
    }
}
