package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.customEvents.CustomEventTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 11/27/17.
 */
public class CustomEventsTriggerModule {

    private static final Logger logger = LoggerFactory.getLogger(CustomEventsTriggerModule.class);
    private CustomEventTrigger customEventTrigger;

    public CustomEventTrigger getCustomEventTrigger(){
        return customEventTrigger;
    }

    public void initCustomEventsTrigger(Map<String, ?> config, String metricPrefix){
        Map<String, ?> customEvents = (Map) config.get("customEvents");
        if(customEvents !=  null){
            Map<String, ?> customEventsGlobalConfig = (Map) customEvents.get("globalConfig");
            List<Map<String, ?>> customEventMetricsList = (List) customEvents.get("metrics");
            if(customEventsGlobalConfig != null && customEventMetricsList != null) {
                logger.info("The CustomEventsTrigger is initialized");
                customEventTrigger = new CustomEventTrigger(customEventsGlobalConfig, customEventMetricsList, metricPrefix);
            }
        }
        else{
            logger.info("The CustomEventsTrigger is not initialized.");
        }
    }
}
