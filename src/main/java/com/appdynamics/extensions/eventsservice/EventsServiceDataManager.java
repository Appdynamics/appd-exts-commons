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

package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.collect.Lists;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import static com.appdynamics.extensions.http.HttpClientUtils.logger;

/**
 * This class is an SDK for developers to communicate with the AppDynamics Events Service. It supports CRUD operations
 * for Schemas, batch publishing of Events and querying for events.
 *
 * @author : Aditya Jagtiani
 * @since : 2.2.0
 */

public class EventsServiceDataManager {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceDataManager.class);
    private Map<String, ?> eventsServiceParameters;
    private CloseableHttpClient httpClient;
    // #TODO HttpResponse is a shared object.Need to check this.
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
        boolean useSSL = (Boolean) eventsServiceParameters.get("useSSL");
        httpClient = Http4ClientBuilder.getBuilder(eventsServiceParameters).build();
        httpHost = new HttpHost(eventsServiceHost, eventsServicePort, useSSL ? "https" : "http");
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
            httpResponse = executeHttpPost(buildRequestUri(schemaName, SCHEMA_PATH), schemaBody);
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
        HttpGet httpGet = new HttpGet(buildRequestUri(schemaName, SCHEMA_PATH));
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
            HttpPatch httpPatch = new HttpPatch(buildRequestUri(schemaName, SCHEMA_PATH));
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
            HttpDelete httpDelete = new HttpDelete(buildRequestUri(schemaName, SCHEMA_PATH));
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
            httpResponse = executeHttpPost(buildRequestUri(schemaName, PUBLISH_PATH), batchBody);
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
    /**
     * This method is used to query events from the Events Service.
     *
     * @param query The required query to be executed on the Events Service.
     */
    public String querySchema(String query) {
        try {
            CloseableHttpResponse httpResponse = executeHttpPost(buildRequestUri("", QUERY_PATH), query);
            if (isResponseSuccessful(httpResponse)) {
                LOGGER.info("Query : {} successful", query);
                return EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            logger.error("Error encountered while querying : " + query, e);
        } finally {
            closeHttpResponse(httpResponse);
        }
        return "";
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