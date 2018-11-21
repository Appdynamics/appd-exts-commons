/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.conf.modules.EventsServiceModule;
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
}