/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.dashboard;

/**
 * Created by bhuvnesh.kumar on 8/7/18.
 */
public class DashboardConstants {


    public static final String REPLACE_MACHINE_PATH = "${MACHINE_PATH}";
    public static final String REPLACE_METRIC_PREFIX = "${METRIC_PREFIX}";
    public static final String REPLACE_APPLICATION_NAME = "${APPLICATION_NAME}";
    public static final String REPLACE_TIER_NAME = "${TIER_NAME}";
    public static final String REPLACE_NODE_NAME = "${NODE_NAME}";
    public static final String REPLACE_DASHBOARD_NAME = "${DASHBOARD_NAME}";
    public static final String REPLACE_SIM_APPLICATION_NAME = "${SIM_APPLICATION_NAME}";
    public static final String REPLACE_HOST_NAME = "${HOST_NAME}";
    public static final String DASHBOARD_NAME = "dashboardName";
    public static final String SIM_APPLICATION_NAME = "Server & Infrastructure Monitoring";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String ROOT = "Root";
    public static final String SIM_DASHBOARD = "simDashboard";
    public static final String NORMAL_DASHBOARD = "normalDashboard";
    public static final String CUSTOM_DASHBOARD = "customDashboard";
    public static final String JSON = "json";
    public static final String APPLICATION_JSON = "application/json";
    public static final String METRICS_SEPARATOR = "|";
    public static final String OVERWRITE_DASHBOARD = "overwriteDashboard";
    public static final int DEFAULT_PERIODIC_DASHBOARD_CHECK_IN_SECONDS = 300;
}
