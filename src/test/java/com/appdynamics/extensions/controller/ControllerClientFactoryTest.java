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

package com.appdynamics.extensions.controller;

import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.yml.YmlReader;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by venkata.konala on 1/2/19.
 */

public class ControllerClientFactoryTest {

    @Test
    public void whenSSLNotEnabledThenShouldUseHttp() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/controller/config.yml"));
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize((Map<String, ?>)config.get("controllerInfo"), PathResolver.resolveDirectory(AManagedMonitor.class));
        ControllerClient controllerClient = ControllerClientFactory.initialize(controllerInfo, (Map<String, ?>)config.get("connection"), (Map<String, ?>)config.get("proxy"),(String)config.get("encryptionKey"));
        Assert.assertNotNull(controllerClient.getBaseURL());
        Assert.assertNotNull(controllerClient.getHttpClient());
        Assert.assertEquals(controllerClient.getBaseURL(), "http://localhost:8090/");

    }

    @Test
    public void whenSSLEnabledThenShouldUseHttps() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/controller/config.yml"));
        ControllerInfo controllerInfo = ControllerInfoFactory.initialize((Map<String, ?>)config.get("controllerInfo"), PathResolver.resolveDirectory(AManagedMonitor.class));
        controllerInfo.setControllerSslEnabled(true);
        ControllerClient controllerClient = ControllerClientFactory.initialize(controllerInfo, (Map<String, ?>)config.get("connection"), (Map<String, ?>)config.get("proxy"),(String)config.get("encryptionKey"));
        Assert.assertNotNull(controllerClient.getBaseURL());
        Assert.assertNotNull(controllerClient.getHttpClient());
        Assert.assertEquals(controllerClient.getBaseURL(), "https://localhost:8090/");

    }
}
