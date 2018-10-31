package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.eventsservice.models.Event;
import com.appdynamics.extensions.eventsservice.models.Schema;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.appdynamics.extensions.eventsservice.utils.EventsServiceUtils.closeHttpResponse;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

/**
 * Author: Aditya Jagtiani
 */
public class EventsServiceDataManager {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceDataManager.class);
    private String monitorName;
    private Map<String, ?> eventsServiceParameters;
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;
    private HttpHost httpHost;
    private String globalAccountName, eventsApiKey;
    static CopyOnWriteArrayList<Event> eventsToBePublished;
    static ConcurrentHashMap<String, String> schemaRegistry;

   public EventsServiceDataManager(String monitorName, Map<String, ?> eventsServiceParameters) {
        this.monitorName = monitorName;
        this.eventsServiceParameters = eventsServiceParameters;
        initialize();
    }

    private void initialize() {
        String eventsServiceHost = (String) eventsServiceParameters.get("host");
        int eventsServicePort = (Integer) eventsServiceParameters.get("port");
        globalAccountName = (String) eventsServiceParameters.get("globalAccountName");
        eventsApiKey = (String) eventsServiceParameters.get("eventsApiKey");
        boolean useSsl = (Boolean) eventsServiceParameters.get("useSsl");
        httpClient = HttpClientBuilder.create().build();
        httpHost = new HttpHost(eventsServiceHost, eventsServicePort, useSsl ? "https" : "http");
    }

    //region <Schema Generation Methods>

    /**
     * A method to generate Schema objects
     * @param schemasToBeGenerated A map containing schema names as keys and paths to schema json files as values
     * @return a List containing the generated Schema objects
     */
    public List<Schema> generateSchema(Map<String, String> schemasToBeGenerated) {
        List<Schema> schemas = Lists.newArrayList();
        for (Map.Entry<String, String> schemaToBeGenerated : schemasToBeGenerated.entrySet()) {
            schemas.add(generateSchema(schemaToBeGenerated.getKey(), schemaToBeGenerated.getValue()));
        }
        return schemas;
    }

    /**
     * A method to generate a Schema object
     * @param schemaName Name of the Schema
     * @param pathToSchemaJson Path to the Schema json file
     * @return a generated Schema object
     */
    public Schema generateSchema(String schemaName, String pathToSchemaJson) {
        File file = new File(pathToSchemaJson);
        try {
            if (file.exists()) {
                LOGGER.info("Building schema: {} from file: {}", schemaName, pathToSchemaJson);
                Schema schema = new Schema();
                schema.setSchemaName(schemaName);
                schema.setSchemaBody(FileUtils.readFileToString(file));
                return schema;
            } else {
                LOGGER.error("Schema file: {} does not exist. Please verify the path to the file", pathToSchemaJson);
            }
        }
        catch (IOException ex) {
            LOGGER.error("Error encountered while reading schema: {} from path: {}", schemaName, pathToSchemaJson);
        }
        return null;
    }
    //endregion

    //region <Schema Registration Methods>

    /**
     * A method to register several Schemas with the Events Service
     * @param schemas A list of Schemas to be registered
     */
    public void registerSchema(List<Schema> schemas) {
        for(Schema schema : schemas) {
            registerSchema(schema);
        }
    }

    /**
     * A method to register a Schema with the Events Service
     * @param schema A Schema to be registered
     */
    public void registerSchema(Schema schema) {
        HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/schema/" + schema.getSchemaName());
        httpPost.setHeader("X-Events-API-AccountName", globalAccountName);
        httpPost.setHeader("X-Events-API-Key", eventsApiKey);
        httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        Gson gson = new Gson();
        String entity = gson.toJson(schema.getSchemaBody());
        try {
            httpPost.setEntity(new StringEntity(entity));
            httpResponse = httpClient.execute(httpPost);
            if(isHttpResponseValid(httpResponse)) {
                LOGGER.info("Schema: {} successfully registered with the Events Service", schema.getSchemaName());
                schemaRegistry.put(schema.getSchemaName(), schema.getSchemaName());
            }
            else {
                LOGGER.error("Schema: {} is invalid. Failed to register with Events Service. Please check the schema body",
                        schema.getSchemaName());
            }
        }
        catch(Exception ex) {
            LOGGER.error("Unable to read the schema: {} as a JSON. Please check if the JSON is valid.", schema.getSchemaName(), ex);
        }
        finally {
            closeHttpResponse(httpResponse);
        }
    }
    //endregion

    //region <Delete Schema Methods>

    /**
     * A method to delete Schemas previously registered with the Events Service
     * @param schemas A list of Schemas to be deleted
     */
    public void deleteSchema(List<Schema> schemas) {
        for(Schema schema: schemas) {
            deleteSchema(schema);
        }
    }

    /**
     * A method to delete a Schema previously registered with the Events Service
     * @param schema A Schema to be deleted
     */
    public void deleteSchema(Schema schema) {
        deleteSchema(schema.getSchemaName());
    }

    /**
     * A method to delete a Schema (by name) previously registered with the Events Service
     * @param schemaName The name of the schema to be deleted
     */
    public void deleteSchema(String schemaName) {
        HttpDelete httpDelete = new HttpDelete(httpHost.toURI() + "/events/schema/" + schemaName);
        httpDelete.setHeader("X-Events-API-AccountName", globalAccountName);
        httpDelete.setHeader("X-Events-API-Key", eventsApiKey);
        try {
             httpResponse = httpClient.execute(httpDelete);
            if (isHttpResponseValid(httpResponse)) {
                LOGGER.info("Schema: {} deleted successfully", schemaName);
                schemaRegistry.remove(schemaName);
            }
            else {
                LOGGER.error("Schema {} does not exist. Please verify the schema name.");
            }
        }
        catch (Exception ex) {
            LOGGER.error("Unable to delete schema: {}", schemaName, ex);
        }
    }
    //endregion

    // todo: update schema patch logic

    //region <Event Generation Methods>
    /**
     * A method to generate Events
     * @param eventsToBeGenerated A map containing schema names as keys and paths to event json files as values
     * @return A list of Events to be published
     */
    public void generateEvent(Map<String, String> eventsToBeGenerated) {
        for(Map.Entry<String, String> event: eventsToBeGenerated.entrySet()) {
            generateEvent(event.getKey(), event.getValue());
        }
    }

    public void generateEvent(String schemaName, String pathToEventJson) {
        File file = new File(pathToEventJson);
        try {
            if (file.exists()) {
                LOGGER.info("Building events for schema: {} from file: {}", schemaName, pathToEventJson);
                JsonNode nodes = new ObjectMapper().readTree(file);
                for(JsonNode node: nodes) {
                    Event event = new Event();
                    event.setSchemaName(schemaName);
                    event.setEventBody(node);
                    eventsToBePublished.add(event);
                }
            } else {
                LOGGER.error("Event file: {} does not exist. Please verify the path to the file", pathToEventJson);
            }
        }
        catch (IOException ex) {
            LOGGER.error("Error encountered while reading events for schema: {} from path: {}", schemaName, pathToEventJson, ex);
        }
    }
    //endregion

    //region <Event Publishing Methods>
    /**
     * A method for batch publishing of events
     */
    public void publishAllEvents() {
        List<List<Event>> batches = Lists.partition(eventsToBePublished, 1000);
        for(List<Event> batch: batches) {
            createSchemaBasedSubBatches(batch);

        }
    }

    private void createSchemaBasedSubBatches(List<Event> batch) {
        Collection<List<Event>> schemaBasedSubBatches = batch.stream().collect(collectingAndThen(groupingBy(event -> event.getSchemaName()), Map::values));

    }

    public void publishEvent(Event event) {
        if(isSchemaRegistered(event.getSchemaName())) {
            HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/publish/" + event.getSchemaName());
            httpPost.setHeader("X-Events-API-AccountName", globalAccountName);
            httpPost.setHeader("X-Events-API-Key", eventsApiKey);
            httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
            Gson gson = new Gson();
            String entity = gson.toJson(event.getEventBody());
            try {
                httpPost.setEntity(new StringEntity(entity));
                httpResponse = httpClient.execute(httpPost);
                if(isHttpResponseValid(httpResponse)) {
                    LOGGER.info("Event successfully published for schema: {}", event.getSchemaName());
                }
                else {
                    LOGGER.error("There was an error in publishing an Event for schema: {}",
                            event.getSchemaName());
                }
            }
            catch(Exception ex) {
                LOGGER.error("Exception occurred while publishing event for Schema: {}", event.getSchemaName(), ex);
            }
            finally {
                closeHttpResponse(httpResponse);
            }
        }
        else {
            LOGGER.error("Schema: {} does not exist. Please register your Schema first.", event.getSchemaName());
        }
    }

    public Object executeAllQueries() {
        if(eventsServiceParameters.containsKey("queryParameters")) {
            LOGGER.info("Executing queries..");

            HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/query");
            httpPost.setHeader("X-Events-API-AccountName", globalAccountName);
            httpPost.setHeader("X-Events-API-Key", eventsApiKey);
            httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
            Gson gson = new Gson();

        }
    }


    //endregion

    //region <Utilities>
    private boolean isSchemaRegistered(String schemaName) {
        return schemaRegistry.containsKey(schemaName);
    }

    private boolean isHttpResponseValid(CloseableHttpResponse httpResponse) {
        return httpResponse != null && httpResponse.getStatusLine() != null &&
                (httpResponse.getStatusLine().getStatusCode() == 201 || httpResponse.getStatusLine().getStatusCode() == 200);
    }
    //endregion
}


