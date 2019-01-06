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
 * Created by venkata.konala on 1/2/19.
 */
public class MetricAPIService extends APIService{
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricAPIService.class);

    MetricAPIService() {
    }

    @Override
    void setControllerInfo(ControllerInfo controllerInfo) {
        this.controllerInfo = controllerInfo;
    }

    @Override
    void setControllerClient(ControllerClient controllerClient) {
        this.controllerClient = controllerClient;
    }

    public JsonNode getMetricData(String applicationName, String metricPathEndPoint) {
        if(controllerClient != null) {
            JsonNode metricDataNode = null;
            String metricData;
            try {
                StringBuilder sb = new StringBuilder("controller/rest/applications/");
                sb.append(applicationName).append(metricPathEndPoint);
                metricData = controllerClient.sendGetRequest(sb.toString());
                metricDataNode = new ObjectMapper().readTree(metricData);
            } catch (ControllerHttpRequestException e) {
                logger.error("Invalid response from controller while fetching information about all dashboards", e);
            } catch (IOException e) {
                logger.error("Error while getting all dashboards information", e);
            }
            return metricDataNode;
        }
        logger.debug("The controllerClient is not initialized");
        return null;
    }
}
