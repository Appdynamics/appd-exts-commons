package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

public class ControllerInfoFactory {

    private static ControllerInfo controllerInfo;
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);

    /*
       This controllerInfo is currently being called in a single threaded environment
       1st priority : System Properties
       2nd priority : Controller Info xml
       3rd priority : config.yml
       yml's controllerInfo overrides system properties which in turn overrides controller-info.xml
     */
    public static ControllerInfo getControllerInfo(Map config){
        if(controllerInfo == null){
            ControllerInfo controllerInfoFromSystemProps = ControllerInfo.fromSystemProperties();
            logger.debug("The resolved properties from system props are {}", controllerInfoFromSystemProps);

            ControllerInfo controllerInfoFromXml = ControllerInfo.getControllerInfoFromXml();
            logger.debug("The resolved properties from Xml are {}", controllerInfoFromXml);
            ControllerInfo mergedInfo = controllerInfoFromSystemProps.merge(controllerInfoFromXml);
            Map ymlConfig = (Map)config.get("controllerInfo");
            if(ymlConfig != null) {
                ControllerInfo controllerInfoFromYml = ControllerInfo.fromYml(ymlConfig);
                logger.debug("The resolved properties from yml are {}", controllerInfoFromYml);
                mergedInfo = mergedInfo.merge(controllerInfoFromYml);

            }

            logger.debug("The resolved properties for ControllerInfo are {}", mergedInfo);
            controllerInfo = mergedInfo;
        }
        return controllerInfo;
    }

}
