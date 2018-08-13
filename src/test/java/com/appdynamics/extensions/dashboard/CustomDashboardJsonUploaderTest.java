/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.TaskInputArgs;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 7/19/18.
 */
public class CustomDashboardJsonUploaderTest {

    @Test
    public void testUploadDashboardJson() throws Exception{
        Map<String, ? super Object> argsMap = new HashMap<>();

        List<Map<String, ?>> serverList = new ArrayList<>();
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, "");
        serverMap.put(TaskInputArgs.PORT, "");
        serverMap.put(TaskInputArgs.USE_SSL, false);
        serverMap.put(TaskInputArgs.USER, "");
        serverMap.put(TaskInputArgs.PASSWORD, "");
        serverList.add(serverMap);
        argsMap.put("servers", serverList);

        Map<String, ? super Object> connectionMap = new HashMap<>();
        String[] sslProtocols = {"TLSv1.2"};
        connectionMap.put(TaskInputArgs.SSL_PROTOCOL, sslProtocols);
        connectionMap.put("sslCertCheckEnabled", false);
        connectionMap.put("connectTimeout", 10000);
        connectionMap.put("socketTimeout", 15000);
        argsMap.put("connection", connectionMap);

        String dashboardString = FileUtils.readFileToString(new File("src/test/resources/dashboard/dashboard.json"));
        CustomDashboardJsonUploader customDashboardJsonUploader = new CustomDashboardJsonUploader();
        customDashboardJsonUploader.uploadDashboard("Dashboard Name", dashboardString, argsMap, false);

    }
}
