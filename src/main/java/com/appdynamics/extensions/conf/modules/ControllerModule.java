package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import com.appdynamics.extensions.controller.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerModule.class);
    private ControllerInfo controllerInfo;
    private ControllerClient controllerClient;

    /**
     * This method initializes the ControllerInfo instance from the ControllerInfoFactory and then uses
     * the ControllerInfo instance to create a HttpClient to the Controller, viz. ControllerClient
     * @param installDir    It refers to the machine agent home folder.
     * @param config        The controllerInfo section form the config.yml resolved as a map
     * @param metricPrefix  The metricPrefix resolved by the MonitorContextConiguration class
     *
     * */
    public void initController(File installDir, Map<String, ?> config, String metricPrefix) {
        Map controllerInfoMap = (Map) config.get("controllerInfo");
        if (controllerInfoMap != null && !controllerInfoMap.isEmpty()) {
            ControllerInfoFactory.initialize(controllerInfoMap, installDir);
            controllerInfo = ControllerInfoFactory.getControllerInfo();
            controllerClient = new ControllerClient(controllerInfo, (Map<String, ?>)config.get("connection"), (Map<String, ?>)config.get("proxy"));
        } else {
            logger.debug("The 'controllerInfo' section in the config.yml is not present or empty, not initializing the ControllerInfo and ControllerClient");
        }
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    public ControllerClient getControllerClient() {
        return controllerClient;
    }
 }
