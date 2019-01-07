package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.util.AssertUtils;

/**
 * Created by venkata.konala on 12/20/18.
 */
public class ControllerAPIServiceFactory {

    private static final ControllerAPIService controllerAPIService = new ControllerAPIService();

    //#TODO Assert for ControllerClient
    public static ControllerAPIService initialize(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        AssertUtils.assertNotNull(controllerClient, "The ControllerClient is null");
        controllerAPIService.initialize(controllerInfo, controllerClient);
        return controllerAPIService;
    }
}
