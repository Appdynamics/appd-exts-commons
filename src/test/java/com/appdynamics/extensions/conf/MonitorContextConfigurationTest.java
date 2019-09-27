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

package com.appdynamics.extensions.conf;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by venkata.konala on 4/2/18.
 */
public class MonitorContextConfigurationTest {

    //private File installDir = PathResolver.resolveDirectory()
    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("Redis Monitor", "Custom Metrics|Redis", new File(""));

    @Test
    public void configYMLreturnMapAndIntializesContextWhenFilePathIsValidTest(){
        monitorContextConfiguration.loadConfigYml("src/test/resources/conf/config.yml");
        Assert.assertTrue(monitorContextConfiguration.isEnabled());
        Assert.assertTrue(monitorContextConfiguration.getConfigYml() != null);
        Assert.assertTrue(monitorContextConfiguration.getMetricPrefix().equals("Server|Component:AppLevels|Custom Metrics|Redis"));
        Assert.assertTrue(monitorContextConfiguration.getContext() != null);
        Assert.assertFalse(monitorContextConfiguration.getContext().isScheduledModeEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void configYMLreturnExceptionAndDoesNotInitialiseContextWhenFilePathIsNotValidTest(){
        monitorContextConfiguration.loadConfigYml("config.yml");
        Assert.assertTrue(monitorContextConfiguration.getContext() == null);
    }
}
