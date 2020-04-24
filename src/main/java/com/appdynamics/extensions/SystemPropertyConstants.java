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

package com.appdynamics.extensions;

/**
 * Created by venkata.konala on 1/7/19.
 */
public class SystemPropertyConstants {
    public static final String WORKBENCH_MODE_PROPERTY = "appdynamics.agent.monitors.workbench.mode";

    public static final String HEALTHCHECKS_ENABLE_PROPERTY = "appdynamics.agent.monitors.healthchecks.enable";

    public static final String ENCRYPTION_KEY_PROPERTY = "appdynamics.agent.monitors.encryptionKey";
    public static final String PLAIN_TEXT_PROPERTY = "appdynamics.agent.monitors.plainText";

    public static final String CONTROLLER_USERNAME_PROPERTY = "appdynamics.agent.monitors.controller.username";
    public static final String CONTROLLER_PASSWORD_PROPERTY = "appdynamics.agent.monitors.controller.password";
    public static final String CONTROLLER_ENCRYPTED_PASSWORD_PROPERTY = "appdynamics.agent.monitors.controller.encryptedPassword";

    public static final String KEYSTORE_PATH_PROPERTY = "appdynamics.agent.monitors.keystore.path";
    public static final String KEYSTORE_PASSWORD_PROPERTY = "appdynamics.agent.monitors.keystore.password";
    public static final String KEYSTORE_ENCRYPTED_PASSWORD_PROPERTY = "appdynamics.agent.monitors.keystore.encryptedPassword";

    public static final String TRUSTSTORE_PATH_PROPERTY = "appdynamics.agent.monitors.truststore.path";
    public static final String TRUSTSTORE_PASSWORD_PROPERTY = "appdynamics.agent.monitors.truststore.password";
    public static final String TRUSTSTORE_ENCRYPTED_PASSWORD_PROPERTY = "appdynamics.agent.monitors.truststore.encryptedPassword";
}
