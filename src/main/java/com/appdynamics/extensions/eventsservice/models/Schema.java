package com.appdynamics.extensions.eventsservice.models;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: Aditya Jagtiani
 */
public class Schema {
    private String schemaName;
    private String schemaBody;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaBody() {
        return schemaBody;
    }

    public void setSchemaBody(String schemaBody) {
        this.schemaBody = schemaBody;
    }
}
