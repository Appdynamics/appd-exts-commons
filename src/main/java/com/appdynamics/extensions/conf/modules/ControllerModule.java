package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import com.appdynamics.extensions.controller.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.appdynamics.extensions.Constants.ENCRYPTION_KEY;

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerModule.class);
    private ControllerInfo controllerInfo;
    private ControllerClient controllerClient;

    /**
     * This method initializes the ControllerInfo instance which is singleton from the ControllerInfoFactory and then
     * validates it and uses the ControllerInfo instance to create a HttpClient to the Controller, viz. ControllerClient
     * @param installDir    It refers to the machine agent home folder.
     * @param config        The controllerInfo section form the config.yml resolved as a map
     *
     * */
    public void initController(File installDir, Map<String, ?> config) {
        Map controllerInfoMap = (Map) config.get("controllerInfo");
        ControllerInfoFactory.initialize(controllerInfoMap, installDir);
        controllerInfo = ControllerInfoFactory.getControllerInfo();
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        if(controllerInfoValidator.isValidatedAndResolved()) {
            ControllerClientFactory controllerClientFactory = new ControllerClientFactory(controllerInfo,
                    (Map<String, ?>) config.get("connection"), (Map<String, ?>) config.get("proxy"),
                    (String)config.get(ENCRYPTION_KEY));
            controllerClient = controllerClientFactory.getControllerClient();
        } else {
            // #TODO Check if this is ok.
            controllerInfo = null;
            logger.warn("ControllerInfo instance is not validated and resolved.....the ControllerClient is null");
        }
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    public ControllerClient getControllerClient() {
        return controllerClient;
    }
 }
