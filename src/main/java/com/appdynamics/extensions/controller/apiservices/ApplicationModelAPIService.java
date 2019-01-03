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
public class ApplicationModelAPIService extends APIService {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(ApplicationModelAPIService.class);

    ApplicationModelAPIService() {
    }

    @Override
    void setControllerInfo(ControllerInfo controllerInfo) {
        this.controllerInfo = controllerInfo;
    }

    @Override
    void setControllerClient(ControllerClient controllerClient) {
        this.controllerClient = controllerClient;
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
}
