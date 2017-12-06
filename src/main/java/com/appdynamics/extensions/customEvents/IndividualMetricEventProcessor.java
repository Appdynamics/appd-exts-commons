package com.appdynamics.extensions.customEvents;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import java.net.URL;

/**
 * Created by venkata.konala on 11/30/17.
 */
public class IndividualMetricEventProcessor {

    private ControllerInfo controllerInfo;
    private String metric;
    private String value;
    private EventParameters eventParameters;

    public IndividualMetricEventProcessor(ControllerInfo controllerInfo, String metric, String value, EventParameters eventParameters){
        this.controllerInfo = controllerInfo;
        this.metric = metric;
        this.value = value;
        this.eventParameters = eventParameters;
    }

    public void processCustomEvents(){
        if(thresholdCrossedCheck(value, eventParameters.getOperator(), eventParameters.getThreshold())){
            try {
                URL url = CustomEventBuilder.createEvent(controllerInfo, eventParameters);
            }
            catch(Exception e){

            }
        }
    }

    private boolean thresholdCrossedCheck(String value, String operator, String threshold){

        switch(operator){
            case ">" : return Integer.parseInt(value) > Integer.parseInt(threshold) ? true : false ;
            case "<" : return Integer.parseInt(value) < Integer.parseInt(threshold) ? true : false ;
            case ">=" : return Integer.parseInt(value) >= Integer.parseInt(threshold) ? true : false ;
            case "<=" : return Integer.parseInt(value) <= Integer.parseInt(threshold) ? true : false ;
            case "==" : return Integer.parseInt(value) == Integer.parseInt(threshold) ? true : false ;
            case "!=" : return Integer.parseInt(value) != Integer.parseInt(threshold) ? true : false ;
            default: break;
        }
        return false;
    }
}
