package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;

/**
 * Created by venkata.konala on 1/2/19.
 */
public class ControllerAPIService {

    private static ApplicationModelAPIService applicationModelAPIService;
    private static CustomDashboardAPIService customDashboardAPIService;
    private static MetricAPIService metricAPIService;

    ControllerAPIService() {
    }

    //#TODO Pass in the dependencies in the abstract super class itself
    void initialize(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        applicationModelAPIService = new ApplicationModelAPIService();
        applicationModelAPIService.setControllerInfo(controllerInfo);
        applicationModelAPIService.setControllerClient(controllerClient);

        customDashboardAPIService = new CustomDashboardAPIService();
        customDashboardAPIService.setControllerInfo(controllerInfo);
        customDashboardAPIService.setControllerClient(controllerClient);

        metricAPIService = new MetricAPIService();
        metricAPIService.setControllerInfo(controllerInfo);
        metricAPIService.setControllerClient(controllerClient);
    }

    public ApplicationModelAPIService getApplicationModelAPIService() {
        return applicationModelAPIService;
    }

    public CustomDashboardAPIService getCustomDashboardAPIService() {
        return customDashboardAPIService;
    }

    public MetricAPIService getMetricAPIService() {
        return metricAPIService;
    }
}
