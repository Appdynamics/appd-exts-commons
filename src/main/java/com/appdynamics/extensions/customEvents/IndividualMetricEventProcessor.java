package com.appdynamics.extensions.customEvents;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by venkata.konala on 11/30/17.
 */
public class IndividualMetricEventProcessor {

    private static Logger logger = LoggerFactory.getLogger(IndividualMetricEventProcessor.class);
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

    public URL processCustomEvents(){
        URL url = null;
        if(thresholdCrossedCheck(value, eventParameters.getOperator(), eventParameters.getThreshold())){

            try {
                url = CustomEventBuilder.createEvent(controllerInfo, eventParameters);
            }
            catch(Exception e){
                logger.debug(e.getMessage());
            }

        }
        return url;
    }

    private boolean thresholdCrossedCheck(String value, String operator, String threshold){
        switch(operator){
            case ">" : return Integer.parseInt(value) > Integer.parseInt(threshold) ;
            case "<" : return Integer.parseInt(value) < Integer.parseInt(threshold);
            case ">=" : return Integer.parseInt(value) >= Integer.parseInt(threshold) ? true : false ;
            case "<=" : return Integer.parseInt(value) <= Integer.parseInt(threshold) ? true : false ;
            case "==" : return Integer.parseInt(value) == Integer.parseInt(threshold) ? true : false ;
            case "!=" : return Integer.parseInt(value) != Integer.parseInt(threshold) ? true : false ;
            default: break;
        }
        return false;
    }
}
