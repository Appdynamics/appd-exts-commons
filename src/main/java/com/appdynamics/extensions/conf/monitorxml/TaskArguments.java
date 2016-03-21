package com.appdynamics.extensions.conf.monitorxml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by abey.tom on 3/16/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskArguments {
    @XmlElement(name = "argument")
    private Argument[] arguments;

    public Argument[] getArguments() {
        return arguments;
    }

    public void setArguments(Argument[] arguments) {
        this.arguments = arguments;
    }
}
