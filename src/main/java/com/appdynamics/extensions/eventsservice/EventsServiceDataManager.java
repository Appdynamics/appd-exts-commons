package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.conf.modules.DerivedMetricsModule;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.unix4j.unix.Echo;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.appdynamics.extensions.eventsservice.EventsServiceUtils.areEventsServiceParametersValid;

/**
 * Author: Aditya Jagtiani
 */
public class EventsServiceDataManager {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceDataManager.class);
    private String monitorName;
    private Map<String, ?> eventsServiceParameters;
    private CopyOnWriteArrayList<String> schemasToBeRegistered;
    private ConcurrentHashMap<String, String> eventsToBePublished;
    private CloseableHttpClient httpClient;
    private volatile AtomicBoolean isSchemaRegistered = new AtomicBoolean(false);

    public EventsServiceDataManager(String monitorName, Map<String, ?> eventsServiceParameters) {
        this.monitorName = monitorName;
        this.eventsServiceParameters = eventsServiceParameters;
    }

    public void registerSchema(List<Map<String, String>> schemaParameters) {
        try {
            String currentSchema;
            for(Map<String, String> schemaParameter: schemaParameters) {
                File file = new File(schemaParameter.get("pathToSchemaJson"));
                if(file.exists()) {
                    currentSchema = FileUtils.readFileToString(file);
                }
                else {
                    LOGGER.error("Unable to read contents of schema: {}", file.getName());
                }
            }
        }
        catch(IOException ex) {

        }
    }









    //initialize stuff

    //Register event schema - make sure this happens once per extension run (flag like dashboards)
    //use an atomic volatile boolean for this check

    //Publish events to ES (batch processing)
    // store all events in a container. Call publish from onComplete/onRunComplete in batches of 1000. Use a concurrent hashmap to
    // prevent data conflicts from simultaneously running tasks. PS: there is only one ibstabce of MWH

    // this has to be done as an SDK with several levels of granularity. A customer can wish to use multiple schemas.
    // Have public methods that handle everything, similar to printMetric.
    // ex - call publish with a list of schemas, which can then call publish for each schema. Make it conform to how an SDK should be.

    // The most granular methods for delete, register and publish should sit in a separate singleton and make the HTTP calls on a per entity basis.


    // the most granular print metric method with a single metric should call this class to convert the metric into an 'Event' for a certain schema.
    // Schema is a table, event is a row in the table.
    // publish(Schema, Metric or whatever)

}
