package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.controller.*;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIServiceFactory;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static com.appdynamics.extensions.Constants.ENCRYPTION_KEY;

/**
 * Created by venkata.konala on 12/19/18.
 */
public class ControllerModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ControllerModule.class);
    private ControllerInfo controllerInfo;
    private ControllerClient controllerClient;
    private ControllerAPIService controllerAPIService;

    /**
     * This method initializes the ControllerInfo instance which is singleton from the ControllerInfoFactory and then
     * validates it and uses the ControllerInfo instance to create a HttpClient to the Controller, viz. ControllerClient
     * @param installDir    It refers to the machine agent home folder.
     * @param config        The controllerInfo section form the config.yml resolved as a map
     *
     * */
    public void initController(File installDir, Map<String, ?> config) {
        Map controllerInfoMap = (Map) config.get("controllerInfo");
        controllerInfo = ControllerInfoFactory.initialize(controllerInfoMap, installDir);
        ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
        if(controllerInfoValidator.isValidated()) {
            controllerClient = ControllerClientFactory.initialize(controllerInfo,
                    (Map<String, ?>) config.get("connection"), (Map<String, ?>) config.get("proxy"),
                    (String)config.get(ENCRYPTION_KEY));
            controllerAPIService = ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
        } else {
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

    public ControllerAPIService getControllerAPIService() {
        return controllerAPIService;
    }
 }
