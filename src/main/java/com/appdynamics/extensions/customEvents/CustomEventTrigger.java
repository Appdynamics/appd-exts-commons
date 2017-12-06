package com.appdynamics.extensions.customEvents;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import com.appdynamics.extensions.metrics.derived.OperandsHandler;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by venkata.konala on 11/27/17.
 */
public class CustomEventTrigger {

    private static Logger logger = LoggerFactory.getLogger(CustomEventTrigger.class);
    private ControllerInfo controllerInfo;
    private Map<String, ?> customEventsGlobalConfig;
    private List<Map<String, ?>> customEventsList;
    private String metricPrefix;
    private EventParametersBuilder eventParametersBuilder;


    public CustomEventTrigger(Map<String, ?> customEventsGlobalConfig, List<Map<String, ?>> customEventsList, String metricPrefix){
        this.customEventsGlobalConfig = customEventsGlobalConfig;
        this.customEventsList = customEventsList;
        this.metricPrefix = metricPrefix;
        eventParametersBuilder = new EventParametersBuilder();
    }

    public void initialize(){
        ControllerInfo controllerInfoFromXml = getControllerInfoFromXml();
        controllerInfo = controllerInfoFromXml.merge(ControllerInfo.fromSystemProperties());
    }

    private ControllerInfo getControllerInfoFromXml(){
        File MA_Directory = PathResolver.resolveDirectory(AManagedMonitor.class);
        logger.info("The MA_Directory is resolved to {}", MA_Directory.getAbsolutePath());
        ControllerInfo controllerInfoFromXml = null;
        if (MA_Directory.exists()) {
            File controllerInfo_XML_File = new File(new File(MA_Directory, "conf"), "controller-info.xml");
            if (controllerInfo_XML_File.exists()) {
                controllerInfoFromXml = ControllerInfo.fromXml(controllerInfo_XML_File);
            }
        }
        if (controllerInfoFromXml == null) {
            controllerInfoFromXml = new ControllerInfo();
        }
        return controllerInfoFromXml;
    }

    public void triggerEvents(Map<String, String> metricMap){
        Map<String, Map<String, String>> organisedMap = buildDataStructure(metricMap);
        for(Map<String, ?> individualMetricEvent : customEventsList){
            String metricPath = individualMetricEvent.get("metricPath").toString();
            String metricName = MetricPathUtils.getMetricName(metricPath);
            Map<String, String> metricsWithSameMetricName = organisedMap.get(metricName);
            Map<String, String> matchedMetricsMap = getMatchedMetrics(metricsWithSameMetricName, metricPath);
            if(matchedMetricsMap != null && matchedMetricsMap.size() != 0) {
                EventParameters eventParameters = eventParametersBuilder.buildEventParameters(individualMetricEvent);
                for(Map.Entry<String, String> individualMetric : matchedMetricsMap.entrySet()){
                    String metric = individualMetric.getKey();
                    String value = individualMetric.getValue();
                    IndividualMetricEventProcessor individualMetricEventProcessor = new IndividualMetricEventProcessor(controllerInfo, metric, value, eventParameters);
                    individualMetricEventProcessor.processCustomEvents();
                }

            }
            else{
                logger.debug("{} does not exist in the available metrics", metricName);
            }

        }


    }

    private Map<String, Map<String, String>> buildDataStructure(Map<String, String> baseMetricsMap){
        Map<String, Map<String, String>> organisedMap = Maps.newHashMap();
        for(Map.Entry<String, String> baseMetric : baseMetricsMap.entrySet()){
            String key = baseMetric.getKey();
            String pathWithoutPrefix = key.replace(metricPrefix, "");
            String value = baseMetric.getValue();
            if(key != null && value != null){
                String metricName = MetricPathUtils.getMetricName(key);
                if(organisedMap.containsKey(metricName)){
                    Map<String, String> organisedMapValue = organisedMap.get(metricName);
                    organisedMapValue.put(pathWithoutPrefix, value);
                }
                else{
                    Map<String, String> organisedMapValue = Maps.newHashMap();
                    organisedMapValue.put(pathWithoutPrefix, value);
                    organisedMap.put(metricName, organisedMapValue);
                }
            }
        }
        return organisedMap;
    }

    private Map<String, String> getMatchedMetrics(Map<String, String> metricsWithSameMetricName, String metricPath){
        Map<String, String> matchedMetrics = Maps.newHashMap();
        if(metricsWithSameMetricName != null) {
            for (Map.Entry<String, String> individualMetric : metricsWithSameMetricName.entrySet()) {
                String key = individualMetric.getKey();
                String value = individualMetric.getValue();
                if (OperandsHandler.match(metricPath, key)) {
                    matchedMetrics.put(key, value);
                }
            }
        }
        return matchedMetrics;
    }



}
