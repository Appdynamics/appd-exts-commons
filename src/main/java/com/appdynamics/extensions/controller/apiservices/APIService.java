package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

/**
 * Created by venkata.konala on 1/1/19.
 */
public abstract class APIService {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(APIService.class);
    protected ControllerInfo controllerInfo;
    protected ControllerClient controllerClient;

    abstract void setControllerInfo(ControllerInfo controllerInfo);

    abstract void setControllerClient(ControllerClient controllerClient);
}
