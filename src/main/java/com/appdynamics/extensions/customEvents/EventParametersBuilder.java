package com.appdynamics.extensions.customEvents;

import java.util.Map;

/**
 * Created by venkata.konala on 12/1/17.
 */
public class EventParametersBuilder {

    public EventParameters buildEventParameters(Map<String, ?> individualMetricEvent){
        EventParameters eventParameters = new EventParameters();
        eventParameters.setComment(individualMetricEvent.get("comment") == null ? "" : individualMetricEvent.get("comment").toString());
        eventParameters.setEventType(individualMetricEvent.get("eventtype") == null ? "" : individualMetricEvent.get("eventtype").toString());
        eventParameters.setSeverity(individualMetricEvent.get("severity") == null ? "" : individualMetricEvent.get("severity").toString());
        eventParameters.setSummary(individualMetricEvent.get("summary") == null ? "" : individualMetricEvent.get("summary").toString());
        return eventParameters;
    }

}
