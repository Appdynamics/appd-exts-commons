package com.appdynamics.extensions.conf;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

public class ControllerInfoFactory {

    private static ControllerInfo controllerInfo;
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerInfoFactory.class);

    /*
       This controllerInfo is currently being called in a single threaded environment
       1st priority : System Properties
       2nd priority : Controller Info xml
       3rd priority : config.yml

     */
    public static ControllerInfo getControllerInfo(Map config, File installDir) {
        if (controllerInfo == null) {
            ControllerInfo controllerInfoFromSystemProps = ControllerInfo.fromSystemProperties();
            logger.debug("The resolved properties from system props are {}", controllerInfoFromSystemProps);

            ControllerInfo controllerInfoFromXml = ControllerInfo.getControllerInfoFromXml(installDir);
            logger.debug("The resolved properties from Xml are {}", controllerInfoFromXml);
            ControllerInfo mergedInfo = controllerInfoFromXml.merge(controllerInfoFromSystemProps);
            if (config != null) {
                ControllerInfo controllerInfoFromYml = ControllerInfo.fromYml(config);
                logger.debug("The resolved properties from yml are {}", controllerInfoFromYml);
                mergedInfo = controllerInfoFromYml.merge(mergedInfo);
            }

            logger.debug("The resolved properties for ControllerInfo are {}", mergedInfo);
            controllerInfo = mergedInfo;
        }
        return controllerInfo;
    }


}
