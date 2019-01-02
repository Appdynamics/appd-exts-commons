package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Created by venkata.konala on 1/1/19.
 */

// #TODO change the name to ApplicationModel API
public class AppTierNodeAPIService extends APIService {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(AppTierNodeAPIService.class);

    private AppTierNodeAPIService() {
    }

    private static AppTierNodeAPIService appTierNodeAPIService = new AppTierNodeAPIService();

    static AppTierNodeAPIService getInstance() {
        return appTierNodeAPIService;
    }

    @Override
    void setControllerInfo(ControllerInfo controllerInfo) {
        appTierNodeAPIService.controllerInfo = controllerInfo;
    }

    @Override
    void setControllerClient(ControllerClient controllerClient) {
        appTierNodeAPIService.controllerClient = controllerClient;
    }

    public JsonNode getSpecificTierNode(String applicationName, String tierName) {
        if(controllerClient != null) {
            JsonNode specificTierNode = null;
            String specificTierData;
            try {
                StringBuilder sb = new StringBuilder("controller/rest/applications/");
                sb.append(applicationName)
                        .append("/tiers/").append(tierName)
                        .append("?output=JSON");
                specificTierData = controllerClient.sendGetRequest(sb.toString());
                specificTierNode = new ObjectMapper().readTree(specificTierData);
            } catch (ControllerHttpRequestException e) {
                logger.error("Invalid response from controller while fetching information about all dashbards", e);
            } catch (IOException e) {
                logger.error("Error while getting all dashboards information", e);
            }
            return specificTierNode;
        }
        logger.debug("The controllerClient is not initialized");
        return null;
    }

    // #TODO Move to MetricAPIService
    public JsonNode getMetricData(String applicationName, String metricPathEndPoint) {
        if(controllerClient != null) {
            JsonNode MetricDataNode = null;
            String metricData;
            try {
                StringBuilder sb = new StringBuilder("controller/rest/applications/");
                sb.append(applicationName).append(metricPathEndPoint);
                metricData = controllerClient.sendGetRequest(sb.toString());
                MetricDataNode = new ObjectMapper().readTree(metricData);
            } catch (ControllerHttpRequestException e) {
                logger.error("Invalid response from controller while fetching information about all dashboards", e);
            } catch (IOException e) {
                logger.error("Error while getting all dashboards information", e);
            }
            return MetricDataNode;
        }
        logger.debug("The controllerClient is not initialized");
        return null;
    }
}
