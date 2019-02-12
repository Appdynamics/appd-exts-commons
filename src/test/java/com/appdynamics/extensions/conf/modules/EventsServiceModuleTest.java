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

import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author : Aditya Jagtiani
 */
public class EventsServiceModuleTest {

    private EventsServiceModule eventsServiceModule;
    private Map<String, ?> config;

    @Before
    public void initialize() {
        eventsServiceModule = new EventsServiceModule();
    }

    @Test
    public void testInitializationWithValidEventsServiceParameters() {
        config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        eventsServiceModule.initEventsServiceDataManager("testMonitor", config);
        Assert.assertThat(eventsServiceModule.getEventsServiceDataManager(), instanceOf(EventsServiceDataManager.class));
    }

    @Test(expected = RuntimeException.class)
    public void testInitializationWithInvalidEventsServiceParameters() {
        config = YmlReader.readFromFile(new File("src/test/resources/conf/" +
                "config_withInvalidEventsServiceParameters.yml"));
        eventsServiceModule.initEventsServiceDataManager("testMonitor", config);
        eventsServiceModule.getEventsServiceDataManager();
    }

    @Test(expected = RuntimeException.class)
    public void testInitializationWithoutEventsServiceParameters() {
        config = YmlReader.readFromFile(new File("src/test/resources/conf/" +
                "config_withoutEventsServiceParameters.yml"));
        eventsServiceModule.initEventsServiceDataManager("testMonitor", config);
        eventsServiceModule.getEventsServiceDataManager();
    }
}