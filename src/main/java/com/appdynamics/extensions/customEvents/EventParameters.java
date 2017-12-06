package com.appdynamics.extensions.customEvents;

import com.google.common.base.Strings;

/**
 * Created by venkata.konala on 12/1/17.
 */
public class EventParameters {

    private String threshold;
    private String operator;
    private String summary;
    private String comment;
    private String eventType;
    private String severity;

    public String getThreshold(){
        return threshold;
    }

    public void setThreshold(String threshold){

        if(Strings.isNullOrEmpty(threshold)){
            this.threshold = "";
        }
        else {
            this.threshold = threshold;
        }
    }

    public String getOperator(){
        return operator;
    }

    public void setOperator(String operator){

        if(Strings.isNullOrEmpty(operator)){
            this.operator = "";
        }
        else {
            this.operator = operator;
        }
    }


    public String getSummary(){
        return summary;
    }

    public void setSummary(String summary){

        if(Strings.isNullOrEmpty(summary)){
            this.summary = "";
        }
        else {
            this.summary = summary;
        }
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){

        if(Strings.isNullOrEmpty(comment)){
            this.comment = "";

        }
        else {
            this.comment = comment;
        }
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {

        if(Strings.isNullOrEmpty(eventType)) {
            this.eventType = "";
        }
        else{
            this.eventType = eventType;
        }

    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        if(Strings.isNullOrEmpty(severity)){
            this.severity = "";

        }
        else {
            this.severity = severity;
        }
    }

}
