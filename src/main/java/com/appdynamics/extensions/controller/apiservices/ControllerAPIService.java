package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;

/**
 * Created by venkata.konala on 1/2/19.
 */
public class ControllerAPIService {

    private ApplicationModelAPIService applicationModelAPIService;
    private CustomDashboardAPIService customDashboardAPIService;
    private MetricAPIService metricAPIService;

    ControllerAPIService() {
    }

    //#TODO Pass in the dependencies in the abstract super class itself
    void initialize(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        applicationModelAPIService = new ApplicationModelAPIService(controllerInfo, controllerClient);
        customDashboardAPIService = new CustomDashboardAPIService(controllerInfo, controllerClient);
        metricAPIService = new MetricAPIService(controllerInfo, controllerClient);
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

    void setApplicationModelAPIService(ApplicationModelAPIService applicationModelAPIService) {
        this.applicationModelAPIService = applicationModelAPIService;
    }

    void setCustomDashboardAPIService(CustomDashboardAPIService customDashboardAPIService) {
        this.customDashboardAPIService = customDashboardAPIService;
    }

    void setMetricAPIService(MetricAPIService metricAPIService) {
        this.metricAPIService = metricAPIService;
    }
}
