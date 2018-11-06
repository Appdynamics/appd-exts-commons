package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.eventsservice.utils.EventsServiceUtils.closeHttpResponse;

/**
 * Author: Aditya Jagtiani
 */
public class EventsServiceDataManager {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceDataManager.class);
    private Map<String, ?> eventsServiceParameters;
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;
    private HttpHost httpHost;
    private String globalAccountName, eventsApiKey;

    public EventsServiceDataManager(Map<String, ?> eventsServiceParameters) {
        this.eventsServiceParameters = eventsServiceParameters;
        initialize();
    }

    private void initialize() {
        String eventsServiceHost = (String) eventsServiceParameters.get("host");
        int eventsServicePort = (Integer) eventsServiceParameters.get("port");
        globalAccountName = (String) eventsServiceParameters.get("globalAccountName");
        eventsApiKey = (String) eventsServiceParameters.get("eventsApiKey");
        boolean useSsl = (Boolean) eventsServiceParameters.get("useSsl");
        httpClient = Http4ClientBuilder.getBuilder(eventsServiceParameters).build();
        httpHost = new HttpHost(eventsServiceHost, eventsServicePort, useSsl ? "https" : "http");
    }

    //region <Schema Creation Methods>
    public void createSchema(String schemaName, File schemaToBeCreated) {
        try {
            if (schemaToBeCreated.exists()) {
                LOGGER.info("Creating Schema: {} from file: {}", schemaName, schemaToBeCreated.getAbsolutePath());
                createSchema(schemaName, FileUtils.readFileToString(schemaToBeCreated));
            } else {
                LOGGER.error("Schema file: {} does not exist", schemaToBeCreated);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to create schema from file: {}", schemaToBeCreated);
        }
    }

    public void createSchema(String schemaName, String schemaBody) {
        HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/schema/" + schemaName);
        httpPost.setHeader("X-Events-API-AccountName", globalAccountName);
        httpPost.setHeader("X-Events-API-Key", eventsApiKey);
        httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        Gson gson = new Gson();
        String entity = gson.toJson(schemaBody);
        try {
            httpPost.setEntity(new StringEntity(entity));
            httpResponse = httpClient.execute(httpPost);
            if (isHttpResponseValid(httpResponse)) {
                LOGGER.info("Schema: {} successfully created & registered with the Events Service", schemaName);
            } else {
                LOGGER.error("Schema: {} is invalid. Failed to register with Events Service. Please check the schema body",
                        schemaName);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to read the schema: {} as a JSON. Please check if the schema body is valid.", schemaName, ex);
        } finally {
            closeHttpResponse(httpResponse);
        }
    }
    // endregion

    // region <Schema Update Methods>
    public void updateSchema(String schemaName, File updatedSchemaBody) {
        try {
            if (updatedSchemaBody.exists()) {
                LOGGER.info("Creating schema from file: {}", updatedSchemaBody.getAbsolutePath());
                updateSchema(schemaName, FileUtils.readFileToString(updatedSchemaBody));
            } else {
                LOGGER.error("Schema file: {} does not exist", updatedSchemaBody);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to create schema from file: {}", updatedSchemaBody);
        }
    }

    public void updateSchema(String schemaName, String updatedSchemaBody) {
        if (!isSchemaRegistered(schemaName)) {
            LOGGER.error("Schema: {} has not been registered with the Events Service. " +
                    "Please register the schema before trying to update it.");
        } else {
            HttpPatch httpPatch = new HttpPatch(httpHost.toURI() + "/events/schema/" + schemaName);
            httpPatch.setHeader("X-Events-API-AccountName", globalAccountName);
            httpPatch.setHeader("X-Events-API-Key", eventsApiKey);
            httpPatch.setHeader("Accept", "application/vnd.appd.events+json;v=2");
            httpPatch.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
            Gson gson = new Gson();
            String entity = gson.toJson(updatedSchemaBody);
            try {
                httpPatch.setEntity(new StringEntity(entity));
                httpResponse = httpClient.execute(httpPatch);
                if (isHttpResponseValid(httpResponse)) {
                    LOGGER.info("Schema: {} successfully updated.", schemaName);
                } else {
                    LOGGER.error("Invalid HTTP Response while trying to update schema: {}",
                            schemaName);
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to update the schema: {}. Please check whether the schema body is valid.", schemaName, ex);
            } finally {
                closeHttpResponse(httpResponse);
            }
        }
    }
    // endregion

    //region <Schema Deletion Methods>

    /**
     * A method to delete schemas previously registered with the Events Service
     *
     * @param schemasToBeDeleted A list of the names of schemas to be deleted
     */
    public void deleteSchema(List<String> schemasToBeDeleted) {
        for (String schema : schemasToBeDeleted) {
            deleteSchema(schema);
        }
    }

    /**
     * A method to delete a Schema (by name) previously registered with the Events Service
     *
     * @param schemaName The name of the schema to be deleted
     */
    public void deleteSchema(String schemaName) {
        if (!isSchemaRegistered(schemaName)) {
            LOGGER.error("Unable to delete Schema: {} as it does not exist.", schemaName);
        } else {
            HttpDelete httpDelete = new HttpDelete(httpHost.toURI() + "/events/schema/" + schemaName);
            httpDelete.setHeader("X-Events-API-AccountName", globalAccountName);
            httpDelete.setHeader("X-Events-API-Key", eventsApiKey);
            try {
                httpResponse = httpClient.execute(httpDelete);
                if (isHttpResponseValid(httpResponse)) {
                    LOGGER.info("Schema: {} deleted successfully", schemaName);
                } else {
                    LOGGER.error("Schema {} does not exist. Please verify the schema name.");
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to delete schema: {}", schemaName, ex);
            }
        }
    }
    //endregion

    //region <Event Publishing Methods>
    public void publishEvent(String schemaName, File eventsToBePublished) {
        if (isSchemaRegistered(schemaName)) {
            if (eventsToBePublished.exists()) {
                LOGGER.info("Processing Events from file: {}", eventsToBePublished.getAbsolutePath());
                publishAll(schemaName, generateEventsForSchema(eventsToBePublished));
            } else {
                LOGGER.error("File: {} does not exist. Cannot publish events.", eventsToBePublished.getAbsolutePath());
            }
        } else {
            LOGGER.error("Schema: {} is not registered with the Events Service. Register the schema before proceeding.",
                    schemaName);
        }
    }

    public void publishAll(String schemaName, List<String> eventsToBePublished) {
        List<List<String>> eventBatches = Lists.partition(eventsToBePublished, 1000);
        for (List<String> eventBatch : eventBatches) {
            publishEvent(schemaName, eventBatch);
        }
    }

    public void publishEvent(String schemaName, List<String> eventsToBePublished) {
        HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/publish/" + schemaName);
        httpPost.setHeader("X-Events-API-AccountName", globalAccountName);
        httpPost.setHeader("X-Events-API-Key", eventsApiKey);
        httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        Gson gson = new Gson();
        String entity = gson.toJson(eventsToBePublished);
        try {
            httpPost.setEntity(new StringEntity(entity));
            httpResponse = httpClient.execute(httpPost);
            if (isHttpResponseValid(httpResponse)) {
                LOGGER.info("Events successfully published for schema: {}", schemaName);
            } else {
                LOGGER.error("HTTP request failure while publishing events for schema: {}",
                        schemaName);
            }
        } catch (Exception ex) {
            LOGGER.error("Error encountered while publishing events for Schema: {}", schemaName, ex);
        } finally {
            closeHttpResponse(httpResponse);
        }
    }
    //endregion

    //region <Querying API>
    public void executeQuery(String query) {

    }

    public void executeQuery(String query, long startTime, long endTime, int limit) {

    }

    public void executeQueries(File queries) {

    }

    //endregion

    //region <Utilities>
    private boolean isHttpResponseValid(CloseableHttpResponse httpResponse) {
        return httpResponse != null && httpResponse.getStatusLine() != null &&
                (httpResponse.getStatusLine().getStatusCode() == 202 || httpResponse.getStatusLine().getStatusCode() == 201
                        || httpResponse.getStatusLine().getStatusCode() == 200);
    }

    private List<String> generateEventsForSchema(File eventsFromFile) {
        List<String> eventsToBePublishedForSchema = Lists.newArrayList();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(eventsFromFile);
            ObjectReader objectReader = mapper.reader(new TypeReference<List<String>>() {
            });
            eventsToBePublishedForSchema = objectReader.readValue(arrayNode);
        } catch (Exception ex) {
            LOGGER.error("Error encountered while generating events from file: {}", eventsFromFile.getAbsolutePath(), ex);
        }
        return eventsToBePublishedForSchema;
    }

    private boolean isSchemaRegistered(String schemaName) {
        HttpGet httpGet = new HttpGet(httpHost.toURI() + "/events/schema/" + schemaName);
        httpGet.setHeader("X-Events-API-AccountName", globalAccountName);
        httpGet.setHeader("X-Events-API-Key", eventsApiKey);
        httpGet.setHeader("Accept", "application/vnd.appd.events+json;v=2");
        try {
            httpResponse = httpClient.execute(httpGet);
            return isHttpResponseValid(httpResponse);
        } catch (Exception ex) {
            LOGGER.error("Error encountered while verifying whether Schema: {} exists", schemaName);
        } finally {
            closeHttpResponse(httpResponse);
        }
        return false;
    }
    //endregion
}