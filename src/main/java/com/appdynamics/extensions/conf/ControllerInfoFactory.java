package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

public class ControllerInfoFactory {

    private static ControllerInfo controllerInfo;
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);

    /*
       This controllerInfo is currently being called in a single threaded environment
       yml's controllerInfo overrides system properties which in turn overrides controller-info.xml
     */
    public static ControllerInfo getControllerInfo(Map config){
        if(controllerInfo == null){
            Map ymlConfig = (Map)config.get("controllerInfo");
            ControllerInfo controllerInfoFromYml = ControllerInfo.fromYml(ymlConfig);
            logger.debug("The resolved properties from yml are {}", controllerInfoFromYml);
            ControllerInfo controllerInfoFromSystemProps = ControllerInfo.fromSystemProperties();
            logger.debug("The resolved properties from system props are {}", controllerInfoFromSystemProps);
            ControllerInfo controllerInfoFromXml = ControllerInfo.getControllerInfoFromXml();
            logger.debug("The resolved properties from Xml are {}", controllerInfoFromXml);
            ControllerInfo mergedInfo = controllerInfoFromXml.merge(controllerInfoFromSystemProps).merge(controllerInfoFromYml);
            controllerInfo = mergedInfo;
        }
        return controllerInfo;
    }

}
