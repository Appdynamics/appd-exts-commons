package com.appdynamics.extensions.conf.monitorxml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by abey.tom on 3/16/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Argument {
    @XmlAttribute
    private String name;
    @XmlAttribute(name = "default-value")
    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
