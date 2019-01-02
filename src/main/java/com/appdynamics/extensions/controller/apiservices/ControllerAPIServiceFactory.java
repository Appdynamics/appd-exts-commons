package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;

/**
 * Created by venkata.konala on 12/20/18.
 */
public class ControllerAPIServiceFactory {

    private static AppTierNodeAPIService appTierNodeAPIService;
    private static CustomDashboardAPIService customDashboardAPIService;

    public static void initialize(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        appTierNodeAPIService = AppTierNodeAPIService.getInstance();
        appTierNodeAPIService.setControllerInfo(controllerInfo);
        appTierNodeAPIService.setControllerClient(controllerClient);
        customDashboardAPIService = CustomDashboardAPIService.getInstance();
        customDashboardAPIService.setControllerInfo(controllerInfo);
        customDashboardAPIService.setControllerClient(controllerClient);
    }

    public static AppTierNodeAPIService getAppTierNodeAPIService() {
        return appTierNodeAPIService;
    }

    public static CustomDashboardAPIService getCustomDashboardAPIService() {
        return customDashboardAPIService;
    }
}
