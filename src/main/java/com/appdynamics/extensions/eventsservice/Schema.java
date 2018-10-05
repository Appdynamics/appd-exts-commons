package com.appdynamics.extensions.eventsservice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: Aditya Jagtiani
 */
public class Schema {
    private String schemaName;
    private String schemaContent;
    private AtomicBoolean isRegistered;

    public boolean isRecreateSchema() {
        return recreateSchema;
    }

    public void setRecreateSchema(boolean recreateSchema) {
        this.recreateSchema = recreateSchema;
    }

    public boolean getRecreateSchema() {return recreateSchema;}

    private boolean recreateSchema;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaContent() {
        return schemaContent;
    }

    public void setSchemaContent(String schemaContent) {
        this.schemaContent = schemaContent;
    }

    public AtomicBoolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(AtomicBoolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
