package com.appdynamics.extensions.eventsservice;

import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.httpclient.URI;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import sun.plugin.liveconnect.SecurityContextHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: Aditya Jagtiani
 */
public class EventsServiceDataManager {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(EventsServiceDataManager.class);
    private String monitorName;
    private Map<String, ?> eventsServiceParameters;
    private List<Schema> schemasToBeRegistered;
    private static ConcurrentHashMap<String, String> eventsToBePublished;
    private CloseableHttpClient httpClient;
    private Map<String, String> httpParametersFromYml;
    private Map<String, String> globalEventServiceParametersFromYml;
    private HttpHost httpHost;

    public EventsServiceDataManager(String monitorName, Map<String, ?> eventsServiceParameters) {
        this.monitorName = monitorName;
        this.eventsServiceParameters = eventsServiceParameters;
        this.schemasToBeRegistered = Lists.newArrayList();
        initialize();
    }

    private void initialize() {
        httpParametersFromYml = (Map) eventsServiceParameters.get("httpParameters");
        globalEventServiceParametersFromYml = (Map) eventsServiceParameters.get("globalParameters");
        httpClient = Http4ClientBuilder.getBuilder(httpParametersFromYml).build();
        httpHost = new HttpHost(httpParametersFromYml.get("host"), Integer.valueOf(httpParametersFromYml.get("port")),
                Boolean.valueOf(httpParametersFromYml.get("sslEnabled")).equals(Boolean.TRUE) ? "https" : "http");
    }

    public List<Schema> generateSchema(List<Map<String, String>> schemaParametersFromYml) {
        try {
            for (Map<String, String> schemaParameter : schemaParametersFromYml) {
                File file = new File(schemaParameter.get("pathToSchemaJson"));
                if (file.exists()) {
                    Schema schema = new Schema();
                    schema.setSchemaName(schemaParameter.get("name"));
                    schema.setSchemaContent(FileUtils.readFileToString(file));
                    schema.setIsRegistered(new AtomicBoolean(false));
                    schema.setRecreateSchema(Boolean.valueOf(schemaParameter.get("recreateSchema")));
                    schemasToBeRegistered.add(schema);
                } else {
                    LOGGER.error("File: {} does not exist. Please check the path to the file", schemaParameter.get("pathToSchemaJson"));
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error encountered while reading schema", ex);
        }
        return schemasToBeRegistered;
    }

    public void registerSchema(List<Schema> schemasToBeRegistered) {
        for (Schema schema : schemasToBeRegistered) {
            registerSchema(schema);
        }
    }

    public void registerSchema(Schema schema) {
        HttpGet httpGet = new HttpGet(httpHost.toURI() + "/events/schema/" + schema.getSchemaName());
        httpGet.setHeader("X-Events-API-AccountName", globalEventServiceParametersFromYml.get("globalAccountName"));
        httpGet.setHeader("X-Events-API-Key", globalEventServiceParametersFromYml.get("eventsApiKey"));
        httpGet.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            if(isHttpResponseValid(httpResponse)) {
                schema.setIsRegistered(new AtomicBoolean(true));
                if (!schema.getRecreateSchema()) {
                    LOGGER.info("Schema: {} already exists and override schema = false. Skipping", schema.getSchemaName());
                }
                else {
                    LOGGER.info("Schema: {} already exists and override schema = true. Deleting existing schema and " +
                            "re-registering.", schema.getSchemaName());
                    deleteSchema(schema);
                    registerSchema(schema.getSchemaName(), schema.getSchemaContent());
                }
            }
            else {
                registerSchema(schema.getSchemaName(), schema.getSchemaContent());
            }
        }
        catch (Exception e) {
            LOGGER.error("Error encountered while registering Schema: {}", schema.getSchemaName(), e);
        }
    }

    private void registerSchema(String schemaName, String schemaBody) {
        HttpPost httpPost = new HttpPost(httpHost.toURI() + "/events/schema/" + schemaName);
        httpPost.setHeader("X-Events-API-AccountName", globalEventServiceParametersFromYml.get("globalAccountName"));
        httpPost.setHeader("X-Events-API-Key", globalEventServiceParametersFromYml.get("eventsApiKey"));
        httpPost.setHeader("Content-type", "application/vnd.appd.events+json;v=2");
        Gson gson = new Gson();
        String entity = gson.toJson(schemaBody);
        try {
            httpPost.setEntity(new StringEntity(entity));
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            if(isHttpResponseValid(httpResponse)) {
                LOGGER.info("Schema: {} registered with the Events Service", schemaName);
            }
            else {
                LOGGER.error("Schema: {} invalid. Failed to register with Events Service", schemaName);
            }
        }
        catch(Exception e) {
            LOGGER.error("Unable to read the schema: {} as a JSON. Please check if the JSON is valid.", schemaName);
        }
    }


    public void deleteSchema(Schema schema) {
        HttpDelete httpDelete = new HttpDelete(httpHost.toURI() + "/events/schema/" + schema.getSchemaName());
        httpDelete.setHeader("X-Events-API-AccountName", globalEventServiceParametersFromYml.get("globalAccountName"));
        httpDelete.setHeader("X-Events-API-Key", globalEventServiceParametersFromYml.get("eventsApiKey"));
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
            if (isHttpResponseValid(httpResponse)) {
                LOGGER.info("Schema: {} deleted successfully", schema.getSchemaName());
            }
        }
        catch (Exception e) {
            LOGGER.error("Unable to delete schema: {}", schema.getSchemaName());
        }
    }

    private boolean isHttpResponseValid(CloseableHttpResponse httpResponse) {
        return httpResponse != null && httpResponse.getStatusLine() != null &&
                (httpResponse.getStatusLine().getStatusCode() == 201 || httpResponse.getStatusLine().getStatusCode() == 200);
    }

    // check if schema is registered and if not, register it with the ES. No need to call from onComplete
    // handle recreate schema


    //only batch processing of publishEvent has to be called from onCOmplete
}


