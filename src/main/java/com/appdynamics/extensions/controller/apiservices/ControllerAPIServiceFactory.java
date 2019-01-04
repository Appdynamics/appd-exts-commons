package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;

/**
 * Created by venkata.konala on 12/20/18.
 */
public class ControllerAPIServiceFactory {

    private static ControllerAPIService controllerAPIService;

    public static ControllerAPIService initialize(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        controllerAPIService = new ControllerAPIService();
        controllerAPIService.initialize(controllerInfo, controllerClient);
        return controllerAPIService;
    }
}
