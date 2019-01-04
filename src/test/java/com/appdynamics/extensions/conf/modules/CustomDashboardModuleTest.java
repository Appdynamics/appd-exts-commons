/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;


import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.ControllerInfoFactory;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.google.common.collect.Maps;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Created by bhuvnesh.kumar on 9/19/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ System.class, HttpClientBuilder.class, CustomDashboardModule.class, CustomDashboardUploader.class, ControllerAPIService.class})
@PowerMockIgnore("javax.net.ssl.*")

public class CustomDashboardModuleTest {
    private Map getCustomDashboardMap() {
        Map config = new HashMap<>();
        config.put("enabled", true);
        config.put("dashboardName", "MonitorName");
        config.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");
        config.put("pathToNormalDashboard", "src/test/resources/dashboard/normalDashboard.json");
        config.put("periodicDashboardCheckInSeconds", 30);
        return config;
    }

    private Map getControllerInfoMap() {
        Map config = new HashMap<>();
        config.put("controllerHost", "hostNameYML");
        config.put("controllerPort", 9999);
        config.put("controllerSslEnabled", false);
        config.put("uniqueHostId", "uniqueHostIDYML");
        config.put("account", "accountNameYML");
        config.put("username", "usernameYML");
        config.put("password", "passwordYML");
        config.put("accountAccessKey", "accessKeyYML");
        config.put("applicationName", "applicationNameYML");
        config.put("tierName", "tierNameYML");
        config.put("nodeName", "nodeNameYML");
        config.put("simEnabled", false);
        return config;
    }

    @Test
    public void whenTimeDifferenceGreaterThanThresholdTimeShouldTryToUpload() throws Exception, ControllerHttpRequestException {
        Map config = new HashMap<>();
        config.put("customDashboard", getCustomDashboardMap());
        config.put("controllerInfo", getControllerInfoMap());
        config.put("proxy", Maps.newHashMap());
        String metricPrefix = "Custom Metrics|MonitorName";
        String monitorName = "MonitorName";
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(getControllerInfoMap(), null);
        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        CustomDashboardAPIService customDashboardAPIService = mock(CustomDashboardAPIService.class);
        when(controllerAPIService.getCustomDashboardAPIService()).thenReturn(customDashboardAPIService);
        CustomDashboardUploader customDashboardUploader = mock(CustomDashboardUploader.class);
        PowerMockito.whenNew(CustomDashboardUploader.class).withAnyArguments().thenReturn(customDashboardUploader);
        PowerMockito.doNothing().when(customDashboardUploader).checkAndUpload(isA(String.class), isA(String.class), isA(Map.class), isA(Boolean.class));
        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        customDashboardModule.initCustomDashboard(config, metricPrefix, monitorName, controllerInfo, controllerAPIService);
        customDashboardModule.uploadDashboard();
        verify(customDashboardUploader, atLeastOnce()).checkAndUpload(isA(String.class), isA(String.class), isA(Map.class), isA(Boolean.class));
    }

    @Test
    public void whenTimeDifferenceLessThanThresholdShouldNotTryToUpload() throws Exception, ControllerHttpRequestException {
        Map config = new HashMap<>();
        config.put("customDashboard", getCustomDashboardMap());
        config.put("controllerInfo", getControllerInfoMap());
        config.put("proxy", Maps.newHashMap());
        String metricPrefix = "Custom Metrics|MonitorName";
        String monitorName = "MonitorName";
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize(getControllerInfoMap(), null);
        ControllerAPIService controllerAPIService = mock(ControllerAPIService.class);
        CustomDashboardAPIService customDashboardAPIService = mock(CustomDashboardAPIService.class);
        when(controllerAPIService.getCustomDashboardAPIService()).thenReturn(customDashboardAPIService);
        CustomDashboardUploader customDashboardUploader = mock(CustomDashboardUploader.class);
        PowerMockito.whenNew(CustomDashboardUploader.class).withAnyArguments().thenReturn(customDashboardUploader);
        PowerMockito.doNothing().when(customDashboardUploader).checkAndUpload(isA(String.class), isA(String.class), isA(Map.class), isA(Boolean.class));
        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        customDashboardModule.initCustomDashboard(config, metricPrefix, monitorName, controllerInfo, controllerAPIService);
        customDashboardModule.uploadDashboard();
        verify(customDashboardUploader, times(1)).checkAndUpload(isA(String.class), isA(String.class), isA(Map.class), isA(Boolean.class));
        customDashboardModule.uploadDashboard();
        verify(customDashboardUploader, times(1)).checkAndUpload(isA(String.class), isA(String.class), isA(Map.class), isA(Boolean.class));
    }
}
