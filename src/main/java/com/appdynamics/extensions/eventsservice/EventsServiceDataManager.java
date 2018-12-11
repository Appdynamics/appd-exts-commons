/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.collect.Lists;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.appdynamics.extensions.eventsservice.utils.Constants.*;
import static com.appdynamics.extensions.http.HttpClientUtils.closeHttpResponse;

/**
 * This class is an SDK for developers to communicate with the AppDynamics Events Service. It supports CRUD operations
 * for Schemas and batch publishing of Events.
 *
 * @author : Aditya Jagtiani
 * @since : 2.2.0
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

    //region <Schema Creation>
    /**
     * This method is used to create and register a new Schema with the Events Service
     *
     * @param schemaName Name of the Schema to be created
     * @param schemaBody Body of the Schema to be created
     */
    public void createSchema(String schemaName, String schemaBody) {
        try {
            httpResponse = executeHttpPost(buildRequestUri(schemaName, SCHEMA_PATH_PARAMS), schemaBody);
            if (isResponseSuccessful(httpResponse)) {
                LOGGER.info("Schema: {} successfully created & registered with the Events Service", schemaName);
            } else {
                LOGGER.error("Schema: {} either already exists or is invalid.",
                        schemaName);
            }
        } catch (IOException ex) {
            LOGGER.error("Error encountered while creating Schema: {}", schemaName, ex);
        } finally {
            closeHttpResponse(httpResponse);
        }
    }
    // endregion

    //region <Schema Retrieval>

    /**
     * This method is used to retrieve an existing Schema
     *
     * @param schemaName Name of the Schema to be retrieved
     * @return String representing the Schema body
     */
    public String retrieveSchema(String schemaName) {
        HttpGet httpGet = new HttpGet(buildRequestUri(schemaName, SCHEMA_PATH_PARAMS));
        httpGet.setHeader(ACCOUNT_NAME_HEADER, globalAccountName);
        httpGet.setHeader(API_KEY_HEADER, eventsApiKey);
        httpGet.setHeader(ACCEPT_HEADER, ACCEPTED_CONTENT_TYPE);
        try {
            LOGGER.info("Attempting to retrieve Schema: {}", schemaName);
            httpResponse = httpClient.execute(httpGet);
            if (isResponseSuccessful(httpResponse)) {
                LOGGER.info("Schema: {} found", schemaName);
                return EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (IOException ex) {
            LOGGER.error("Error encountered while retrieving Schema: {}", schemaName);
        } finally {
            closeHttpResponse(httpResponse);
        }
        LOGGER.error("Schema: {} does not exist", schemaName);
        return "";
    }
    //endregion

    // region <Schema Update>
    /**
     * This method is used to update an existing Schema by field, using an HTTP Patch
     *
     * @param schemaName    Name of the Schema to be updated
     * @param schemaUpdates Request body, defining the updates to be applied to the Schema
     */
    public void updateSchema(String schemaName, String schemaUpdates) {
        if (!StringUtils.hasText(retrieveSchema(schemaName))) {
            LOGGER.error("Schema: {} does not exist. Create the schema before proceeding", schemaName);
        } else {
            HttpPatch httpPatch = new HttpPatch(buildRequestUri(schemaName, SCHEMA_PATH_PARAMS));
            httpPatch.setHeader(ACCOUNT_NAME_HEADER, globalAccountName);
            httpPatch.setHeader(API_KEY_HEADER, eventsApiKey);
            httpPatch.setHeader(ACCEPT_HEADER, ACCEPTED_CONTENT_TYPE);
            httpPatch.setHeader(CONTENT_TYPE_HEADER, ACCEPTED_CONTENT_TYPE);
            try {
                httpPatch.setEntity(new StringEntity(schemaUpdates, ContentType.APPLICATION_FORM_URLENCODED));
                httpResponse = httpClient.execute(httpPatch);
                if (isResponseSuccessful(httpResponse)) {
                    LOGGER.info("Schema: {} successfully updated.", schemaName);
                } else {
                    LOGGER.error("Schema update body invalid. Unable to update Schema: {}",
                            schemaName);
                }
            } catch (IOException ex) {
                LOGGER.error("Error encountered while updating Schema: {}", schemaName, ex);
            } finally {
                closeHttpResponse(httpResponse);
            }
        }
    }
    // endregion

    //region <Schema Delete>

    /**
     * This method is used to delete existing Schemas
     *
     * @param schemasToBeDeleted A list of the names of schemas to be deleted
     */
    public void deleteSchema(List<String> schemasToBeDeleted) {
        for (String schema : schemasToBeDeleted) {
            deleteSchema(schema);
        }
    }

    /**
     * This method is used to delete an existing Schema
     *
     * @param schemaName The name of the Schema to be deleted
     */
    public void deleteSchema(String schemaName) {
        if (!StringUtils.hasText(retrieveSchema(schemaName))) {
            LOGGER.error("Schema: {} does not exist. Unable to delete", schemaName);
        } else {
            HttpDelete httpDelete = new HttpDelete(buildRequestUri(schemaName, SCHEMA_PATH_PARAMS));
            httpDelete.setHeader(ACCOUNT_NAME_HEADER, globalAccountName);
            httpDelete.setHeader(API_KEY_HEADER, eventsApiKey);
            try {
                httpResponse = httpClient.execute(httpDelete);
                if (isResponseSuccessful(httpResponse)) {
                    LOGGER.info("Schema: {} deleted successfully", schemaName);
                } else {
                    LOGGER.error("Unable to delete Schema: {}", schemaName);
                }
            } catch (IOException ex) {
                LOGGER.error("Error encountered while deleting Schema: {}", schemaName, ex);
            }
        }
    }
    //endregion

    //region <Event Publishing>
    /**
     * This method is used to publish events to the Events Service, in batches of 1000
     *
     * @param schemaName          Name of the Schema to publish the Events to
     * @param eventsToBePublished A List containing the events to be published
     */
    public void publishEvents(String schemaName, List<String> eventsToBePublished) {
        List<List<String>> eventBatches = Lists.partition(eventsToBePublished, 1000);
        for (List<String> eventBatch : eventBatches) {
            publishBatch(schemaName, eventBatch);
        }
    }

    private void publishBatch(String schemaName, List<String> eventBatch) {
        try {
            String batchBody = eventBatch.stream().collect(Collectors.joining(",", "[", "]"));
            httpResponse = executeHttpPost(buildRequestUri(schemaName, EVENT_PATH_PARAM), batchBody);
            if (isResponseSuccessful(httpResponse)) {
                LOGGER.info("Batch with {} events successfully published for schema: {}", eventBatch.size(), schemaName);
            } else {
                LOGGER.error("One or more events invalid for Schema: {}. Unable to publish", schemaName);
            }
        } catch (IOException ex) {
            LOGGER.error("Error encountered while publishing events for Schema: {}", schemaName, ex);
        } finally {
            closeHttpResponse(httpResponse);
        }
    }
    //endregion

    //region <Utilities>
    private CloseableHttpResponse executeHttpPost(String uri, String requestBody) throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader(ACCOUNT_NAME_HEADER, globalAccountName);
        httpPost.setHeader(API_KEY_HEADER, eventsApiKey);
        httpPost.setHeader(CONTENT_TYPE_HEADER, ACCEPTED_CONTENT_TYPE);
        httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_FORM_URLENCODED));
        return httpClient.execute(httpPost);
    }

    private String buildRequestUri(String schemaName, String pathParams) {
        return httpHost.toURI() + pathParams + schemaName;
    }

    private boolean isResponseSuccessful(CloseableHttpResponse httpResponse) {
        return httpResponse != null && httpResponse.getStatusLine() != null &&
                (httpResponse.getStatusLine().getStatusCode() == 202 || httpResponse.getStatusLine().getStatusCode() == 201
                        || httpResponse.getStatusLine().getStatusCode() == 200);
    }
    //endregion
}