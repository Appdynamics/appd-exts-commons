package com.appdynamics.extensions.customEvents;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import org.apache.http.client.utils.URIBuilder;

import java.net.URL;

/**
 * Created by venkata.konala on 12/1/17.
 */
public class CustomEventBuilder {
    public static URL createEvent(ControllerInfo controllerInfo, EventParameters eventParameters) throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost(controllerInfo.getControllerHost())
                .setPort(controllerInfo.getControllerPort())
                .setPath("/controller/rest/applications/" + controllerInfo.getApplicationName() + "/events")
                .setParameter("summary", eventParameters.getSummary())
                .setParameter("comment", eventParameters.getComment())
                .setParameter("eventtype", eventParameters.getEventType())
                .setParameter("severity", eventParameters.getSeverity());
        return builder.build().toURL();
    }
}
