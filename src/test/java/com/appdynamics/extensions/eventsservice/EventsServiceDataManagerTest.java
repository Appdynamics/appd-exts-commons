package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class EventsServiceDataManagerTest {

    private EventsServiceDataManager eventsServiceDataManager;

    @Before
    public void initialize() {
        Map<String, ?> eventsServiceParams = (Map) YmlReader.readFromFile(new File("src/test/resources/conf/config.yml")).get("eventsServiceParameters");
        eventsServiceDataManager = new EventsServiceDataManager(eventsServiceParams);
    }

    @Test
    public void createSchemaTest() {

    }
}
