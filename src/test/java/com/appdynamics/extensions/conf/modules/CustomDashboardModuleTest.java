/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bhuvnesh.kumar on 9/19/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, HttpClientBuilder.class, CustomDashboardModule.class})
@PowerMockIgnore("javax.net.ssl.*")

public class CustomDashboardModuleTest {

    private Map getCustomDashboardMap() {
        Map config = new HashMap<>();
        config.put("enabled", true);
        config.put("dashboardName", "MonitorName");
        config.put("pathToSIMDashboard", "src/test/resources/dashboard/simDashboard.json");
        config.put("pathToNormalDashboard", "src/test/resources/dashboard/normalDashboard.json");
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
    public void testUploadDashboardForCorrectTimeDifference() throws Exception {

        CustomDashboardModule customDashboardModule = new CustomDashboardModule();
        Map config = new HashMap<>();
        config.put("customDashboard", getCustomDashboardMap());
        config.put("controllerInfo", getControllerInfoMap());
        String metric = "Custom Metrics|MonitorName";
        String monitorName = "MonitorName";
        File file = Mockito.mock(File.class);
        ControllerInfo controllerInfo;
        ControllerInfoFactory.initialize(getControllerInfoMap(), file);
        controllerInfo = ControllerInfoFactory.getControllerInfo();
        customDashboardModule.initCustomDashboard(config, metric, monitorName, controllerInfo);

        PowerMockito.mockStatic(HttpClientBuilder.class);
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        HttpClientBuilder mockHttpClientBuilder = PowerMockito.mock(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.class, "create").thenReturn(mockHttpClientBuilder);
        PowerMockito.when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.currentTimeMillis()).thenReturn(300001l).thenReturn(300011l).
                thenReturn(50000l).thenReturn(50000l).thenReturn(700000l).thenReturn(700000l);

        //first run, normal upload
        AtomicBoolean check = customDashboardModule.getPeriodicPresenceCheck();
        Assert.assertFalse(check.get());
        customDashboardModule.uploadDashboard();
        check = customDashboardModule.getPeriodicPresenceCheck();
        Assert.assertTrue(check.get());
        // 2nd run, no upload
        customDashboardModule.uploadDashboard();
        check = customDashboardModule.getPeriodicPresenceCheck();
        Assert.assertTrue(check.get());
        // 3rd run, greater than 5 mins, upload
        customDashboardModule.uploadDashboard();
        check = customDashboardModule.getPeriodicPresenceCheck();
        Assert.assertTrue(check.get());

    }
}