package com.appdynamics.extensions.eventsservice.models;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author: Aditya Jagtiani
 */
public class Event {

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Object getEventBody() {
        return eventBody;
    }

    public void setEventBody(Object eventBody) {
        this.eventBody = eventBody;
    }

    private String schemaName;
    private Object eventBody;

}
