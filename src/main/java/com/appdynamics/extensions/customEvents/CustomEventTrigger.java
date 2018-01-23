package com.appdynamics.extensions.customEvents;

import com.appdynamics.extensions.dashboard.ControllerInfo;
import com.appdynamics.extensions.metrics.derived.OperandsHandler;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.yml.YmlReader.logger;

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
    private Long startTime;
    private Cache<String, URL> eventsCache= CacheBuilder.newBuilder().build();


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
        ControllerInfo controllerInfoFromXml = new ControllerInfo();
        File MA_Directory = PathResolver.resolveDirectory(AManagedMonitor.class);
        logger.info("The MA_Directory is resolved to {}", MA_Directory.getAbsolutePath());
        if (MA_Directory.exists()) {
            File controllerInfo_XML_File = new File(new File(MA_Directory, "conf"), "controller-info.xml");
            if (controllerInfo_XML_File.exists()) {
                controllerInfoFromXml = ControllerInfo.fromXml(controllerInfo_XML_File);
            }
        }
        return controllerInfoFromXml;
    }

    public void triggerEvents(Map<String, String> metricMap){
        Long currentTime = System.currentTimeMillis();
        if(startTime == null){
            startTime = currentTime;
        }
        Map<String, URL> eventsToBePosted = Maps.newHashMap();
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
                    URL url = individualMetricEventProcessor.processCustomEvents();
                    if(url != null) {
                        eventsToBePosted.put(metric, url);
                    }
                }
                try {
                    uploadCustomEvents(eventsToBePosted, currentTime);
                }
                catch(Exception e){
                    logger.debug(e.getMessage());
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

    private void uploadCustomEvents(Map<String, URL> eventsToBePosted, Long currentTime) throws Exception {
        HttpClient httpClient = getHttpClient();
        if(currentTime - startTime < 180000){
            for (Map.Entry<String, URL> eventToBePosted : eventsToBePosted.entrySet()) {
                eventsCache.put(eventToBePosted.getKey(), eventToBePosted.getValue());
            }
        }
        else{
            for (Map.Entry<String, URL> event : eventsCache.asMap().entrySet()) {
                HttpResponse response = httpClient.execute(new HttpPost(event.getValue().toURI()));
                logger.debug("Custom Event Sent to the Controller, Response Code = " + response.getStatusLine().getStatusCode());
            }

            eventsCache.cleanUp();
            startTime = System.currentTimeMillis();

        }
    }

    private HttpClient getHttpClient() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(controllerInfo.getUsername(), controllerInfo.getPassword());
        provider.setCredentials(AuthScope.ANY, credentials);
        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }


}
