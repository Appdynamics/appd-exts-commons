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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by venkata.konala on 1/3/19.
 */
public class ControllerModuleTest {

    @Test
    public void whenControllerInfoValidatedAllThreeComponentsAreNotNullTest() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/controller/config.yml"));
        ControllerModule controllerModule = new ControllerModule();
        controllerModule.initController(null, conf);
        Assert.assertNotNull(controllerModule.getControllerInfo());
        Assert.assertNotNull(controllerModule.getControllerClient());
        Assert.assertNotNull(controllerModule.getControllerAPIService());

    }

    @Test
    public void whenControllerInfoNotValidatedTwoComponentsAreNullTest() {
        Map<String, ?> conf = YmlReader.readFromFile(new File("src/test/resources/conf/config_withIncompleteControllerInfo"));
        ControllerModule controllerModule = new ControllerModule();
        controllerModule.initController(null, conf);
        Assert.assertNotNull(controllerModule.getControllerInfo());
        Assert.assertNull(controllerModule.getControllerClient());
        Assert.assertNull(controllerModule.getControllerAPIService());

    }
}
