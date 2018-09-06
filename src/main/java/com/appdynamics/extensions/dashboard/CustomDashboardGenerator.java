/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoValidator;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.util.StringUtils;
import com.appdynamics.extensions.xml.Xml;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;

import static com.appdynamics.extensions.dashboard.DashboardConstants.*;

/**
 * Created by abey.tom on 4/10/15.
 */
public class CustomDashboardGenerator {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardGenerator.class);
    public static final String TIER_METRIC_PREFIX = "Server|Component:";
    private Set<String> instanceNames;
    private String metricPrefix;
    private Map dashboardConfig;
    private ControllerInfo controllerInfo;
//    private AgentEnvironmentResolver agentEnvResolver;
    protected CustomDashboardUploader dashboardUploader;

    public Map getHttpArgs() {
        return httpArgs;
    }
    public String getDashboardContent() {
        return dashboardContent;
    }
    public String getDashboardName() {
        return dashboardName;
    }

    private String dashboardContent;
    private Map httpArgs;
    private String dashboardName;

    public CustomDashboardGenerator( Map dashboardConfig, ControllerInfo controllerInformation, String metricPrefix) {
        this.dashboardConfig = dashboardConfig;
        this.controllerInfo = controllerInformation;
        this.metricPrefix = metricPrefix;
    }

    public void createDashboard() {
        if (isResolved()) {
            Map<String, ? super Object> argsMap = getArgsMap();
            String dashboardTemplate = getDashboardContents();
            if(Strings.isNullOrEmpty(dashboardTemplate)){
                logger.error("Dashboard file is empty");
                return;
            }

            setDashboardName();
            dashboardTemplate = setDefaultDashboardInfo(dashboardTemplate);

            this.dashboardContent = dashboardTemplate;
            this.httpArgs = argsMap;


        } else {
            logger.error("Unable to establish connection, please make sure you have provided all necessary values.");
        }

    }

    private void setDashboardName() {
        String dashboardName  ;
        if(!Strings.isNullOrEmpty((String)dashboardConfig.get("dashboardName"))){
            dashboardName = dashboardConfig.get("dashboardName").toString();
        } else {
            dashboardName = "Custom Dashboard";
        }
        dashboardConfig.put("dashboardName", dashboardName);
        this.dashboardName = dashboardName;
    }


    protected boolean isResolved() {
        ControllerInfoValidator validator = new ControllerInfoValidator();

        return validator.validateAndCheckIfResolved(controllerInfo);
    }

    private String getDashboardContents() {
        logger.debug("Sim Enabled: {}", controllerInfo.getSimEnabled());
        String dashboardTemplate = "";
        String pathToFile;
        if (controllerInfo.getSimEnabled() == false) {
            pathToFile = dashboardConfig.get("pathToNormalDashboard").toString();
        } else {
            pathToFile = dashboardConfig.get("pathToSIMDashboard").toString();
        }

        try {
            if (!Strings.isNullOrEmpty(pathToFile)) {
                File file = new File(pathToFile);
                if(file.exists()){
                    dashboardTemplate = FileUtils.readFileToString(file);
                } else {
                    logger.error("Unable to read the contents of the dashboard file: {}", pathToFile);

                }
            } else {
                logger.error("The path to your dashboardFile is empty in your config.yml file.");
            }
        } catch (IOException e) {
            logger.error("Unable to read the contents of the dashboard file: {}", e.getMessage());
        }
        return dashboardTemplate;
    }

    protected Map<String, ? super Object> getArgsMap() {

        Map<String, ? super Object> argsMap = new HashMap<>();

        List<Map<String, ? super Object>> serverList = Lists.newArrayList();
        Map<String, ? super Object> serverMap = getServerMap();
        serverList.add(serverMap);
        argsMap.put("servers", serverList);

        Map<String, ? super Object> connectionMap = getConnectionMap();
        argsMap.put("connection", connectionMap);

        return argsMap;
    }

    private Map<String, ? super Object> getServerMap() {
        Map<String, ? super Object> serverMap = new HashMap<>();
        serverMap.put(TaskInputArgs.HOST, controllerInfo.getControllerHost());
        serverMap.put(TaskInputArgs.PORT, String.valueOf(controllerInfo.getControllerPort()));
        serverMap.put(TaskInputArgs.USE_SSL, String.valueOf(controllerInfo.getControllerSslEnabled()));
        serverMap.put(TaskInputArgs.USER, getUserName());
        serverMap.put(TaskInputArgs.PASSWORD, controllerInfo.getPassword());

        logger.debug("Controller Info: ");
        logger.debug(TaskInputArgs.HOST + ": {}", controllerInfo.getControllerHost());
        logger.debug(TaskInputArgs.PORT + ": {}", String.valueOf(controllerInfo.getControllerPort()));
        logger.debug(TaskInputArgs.USE_SSL + ": {}", controllerInfo.getControllerSslEnabled());
        logger.debug(TaskInputArgs.USER + ": {}", getUserName());
        return serverMap;
    }

    private Map<String, ? super Object> getConnectionMap() {
        Map<String, ? super Object> connectionMap = new HashMap<>();
        String[] sslProtocols = {TLSV_12};
        connectionMap.put(TaskInputArgs.SSL_PROTOCOL, sslProtocols);
        Object sslCertCheckEnabled = dashboardConfig.get(SSL_CERT_CHECK_ENABLED);
        if (sslCertCheckEnabled != null) {
            connectionMap.put(SSL_CERT_CHECK_ENABLED, Boolean.valueOf(sslCertCheckEnabled.toString()));
        } else {
            connectionMap.put(SSL_CERT_CHECK_ENABLED, true);
        }
        connectionMap.put(CONNECT_TIMEOUT, 10000);
        connectionMap.put(SOCKET_TIMEOUT, 15000);
        return connectionMap;
    }

    private String getUserName() {
        String accountName = controllerInfo.getAccount();
        String username = controllerInfo.getUsername();
        if (accountName != null && username != null) {
            return username + AT + accountName;
        }
        return "";
    }

    // TODO String builder
    public String setDefaultDashboardInfo(String dashboardString) {
        dashboardString = setMetricPrefix(dashboardString);
        dashboardString = setApplicationName(dashboardString);
        dashboardString = setSimApplicationName(dashboardString);
        dashboardString = setTierName(dashboardString);
        dashboardString = setNodeName(dashboardString);
        dashboardString = setHostName(dashboardString);
        dashboardString = setDashboardName(dashboardString);
        dashboardString = setMachinePath(dashboardString);
        return dashboardString;

    }
    private String setMetricPrefix(String dashboardString) {
        if (dashboardString.contains(REPLACE_METRIC_PREFIX)) {
            dashboardString = dashboardString.replace(REPLACE_METRIC_PREFIX, metricPrefix);
            logger.debug(REPLACE_METRIC_PREFIX + ": " + metricPrefix);
        }
        return dashboardString;
    }

    private String setApplicationName(String dashboardString) {
        if (dashboardString.contains(REPLACE_APPLICATION_NAME)) {
            dashboardString = dashboardString.replace(REPLACE_APPLICATION_NAME, controllerInfo.getApplicationName());
            logger.debug(REPLACE_APPLICATION_NAME + ": " + controllerInfo.getApplicationName());
        }
        return dashboardString;
    }

    private String setSimApplicationName(String dashboardString) {
        if (dashboardString.contains(REPLACE_SIM_APPLICATION_NAME)) {
            dashboardString = dashboardString.replace(REPLACE_SIM_APPLICATION_NAME, SIM_APPLICATION_NAME);
            logger.debug(REPLACE_SIM_APPLICATION_NAME + ": " + REPLACE_SIM_APPLICATION_NAME);
        }
        return dashboardString;
    }

    private String setTierName(String dashboardString) {
        if (dashboardString.contains(REPLACE_TIER_NAME)) {
            dashboardString = dashboardString.replace(REPLACE_TIER_NAME, controllerInfo.getTierName());
            logger.debug(REPLACE_TIER_NAME + ": " + controllerInfo.getTierName());
        }
        return dashboardString;
    }

    private String setNodeName(String dashboardString) {
        if (dashboardString.contains(REPLACE_NODE_NAME)) {
            dashboardString = dashboardString.replace(REPLACE_NODE_NAME, controllerInfo.getNodeName());
            logger.debug(REPLACE_NODE_NAME + ": " + controllerInfo.getNodeName());
        }
        return dashboardString;
    }

    private String setHostName(String dashboardString) {
        if (dashboardString.contains(REPLACE_HOST_NAME)) {
            dashboardString = dashboardString.replace(REPLACE_HOST_NAME, controllerInfo.getControllerHost());
            logger.debug(REPLACE_HOST_NAME + ": " + controllerInfo.getControllerHost());
        }
        return dashboardString;
    }

    private String setDashboardName(String dashboardString) {
        String dashBoardName = dashboardConfig.get("dashboardName").toString();
        if (!StringUtils.hasText(dashBoardName)) {
            dashBoardName = "Custom Dashboard";
        }

        if (dashboardString.contains(REPLACE_DASHBOARD_NAME)) {
            if (dashboardConfig.get("dashboardName") != null)
                dashboardString = dashboardString.replace(REPLACE_DASHBOARD_NAME, dashBoardName);
            logger.debug(REPLACE_DASHBOARD_NAME + ": " + dashBoardName);
        }
        return dashboardString;
    }

    private String setMachinePath(String dashboardString) {
        if (dashboardString.contains(REPLACE_MACHINE_PATH)) {
            if (controllerInfo.getMachinePath() != null) {
                String machinePath = ROOT + METRICS_SEPARATOR + controllerInfo.getMachinePath();
                machinePath = machinePath.substring(0, machinePath.lastIndexOf(METRICS_SEPARATOR));
                dashboardString = dashboardString.replace(REPLACE_MACHINE_PATH, machinePath);
                logger.debug(REPLACE_MACHINE_PATH + ": " + machinePath);
            } else {
                dashboardString = dashboardString.replace(REPLACE_MACHINE_PATH, ROOT);
                logger.debug(REPLACE_MACHINE_PATH + ": " + ROOT);
            }
        }
        return dashboardString;
    }

    /////////////////////// XML Support ///////////////////////

    private String setDashboardName(Node source, String instanceName) {
        String dashBoardName = (String) dashboardConfig.get("namePrefix");
        if (!StringUtils.hasText(dashBoardName)) {
            dashBoardName = "Custom Dashboard";
        }
        if (StringUtils.hasText(instanceName)) {
            dashBoardName += "-" + instanceName;
        }
        addAttribute(source.getFirstChild(), "name", dashBoardName);
        return dashBoardName;
    }


    public CustomDashboardGenerator(Set<String> instanceNames, String metricPrefix, Map dashboardConfig,  ControllerInfo controllerInfo) {
        if (dashboardConfig == null) {
            logger.info("Custom Dashboard config is null");
            return;
        }
        Boolean enabled = (Boolean) dashboardConfig.get("enabled");
        if (enabled == null || !enabled) {
            logger.info("Custom Dashboard creation is not enabled");
            return;
        }

        this.controllerInfo = controllerInfo;
        this.instanceNames = instanceNames;
        this.metricPrefix = StringUtils.trim(metricPrefix, "|");
        this.dashboardConfig = dashboardConfig;
        ControllerApiService controllerApiService = new ControllerApiService(controllerInfo);
        this.dashboardUploader = new CustomDashboardUploader(controllerApiService);
    }

    public void createDashboards(Collection<String> metrics) {
        if (isResolved()) {
            StringBuilder ctrlMetricPrefix = buildMetricPrefix(metricPrefix);
            metrics = replaceMetricPrefix(metrics, metricPrefix, ctrlMetricPrefix);
            if (logger.isDebugEnabled()) {
                logger.debug("The metrics are {}", metrics);
            }
            for (String instanceName : instanceNames) {
                createDashboard(metrics, instanceName, ctrlMetricPrefix.toString());
            }
        } else {
            logger.error("Cannot create the Custom Dashboard, since the agent resolver failed earlier. Please check the log messages at startup for cause");
        }
    }

    private Collection<String> replaceMetricPrefix(Collection<String> metrics, String metricPrefix, StringBuilder ctrlMetricPrefix) {
        Collection<String> list = new ArrayList<String>();
        for (String metric : metrics) {
            list.add(metric.replace(metricPrefix, ctrlMetricPrefix));
        }
        return list;
    }

    protected StringBuilder buildMetricPrefix(String metricPrefix) {
        StringBuilder ctrlMetricPrefix = new StringBuilder();
        String tierName = controllerInfo.getTierName();
        if (metricPrefix.startsWith(TIER_METRIC_PREFIX)) {
            int endIndex = metricPrefix.indexOf("|", 17);
            if (endIndex == -1) {
                endIndex = metricPrefix.length();
            }
            ctrlMetricPrefix.append("Application Infrastructure Performance|").append(tierName);
            if (metricPrefix.length() - 1 > endIndex) {
                ctrlMetricPrefix.append("|")
                        .append(StringUtils.trim(metricPrefix.substring(endIndex), "|"));
            }
        } else {
            ctrlMetricPrefix.append("Application Infrastructure Performance|")
                    .append(tierName)
                    .append("|").append(StringUtils.trim(metricPrefix, "|"));
        }
        logger.info("The Controller Metric prefix is {}", ctrlMetricPrefix);
        return ctrlMetricPrefix;
    }

    public void createDashboard(Collection<String> metrics, String instanceName, String ctrlMetricPrefix) {
        Map<Node, Node> addMap = new IdentityHashMap<Node, Node>();
        Map<Node, Node> removeMap = new IdentityHashMap<Node, Node>();
        Xml xml = new Xml(getDashboardTemplate());
        String dashboardName = setDashboardName(xml.getSource(), instanceName);
        NodeList widgets = xml.getElementsByTagName("widget-series");
        int seriesNameCount = 0;
        if (widgets != null) {
            for (int i = 0; i < widgets.getLength(); i++) {
                Node widget = widgets.item(i);
                Node node = Xml.getFirstDescendant(widget, "metric-name");
                if (node != null) {
                    String metricTemplate = node.getTextContent();
                    metricTemplate = replacePrefixAndInstanceName(metricTemplate, ctrlMetricPrefix, instanceName);
                    Node widgetSeriesList = widget.getParentNode();
                    removeMap.put(widget, widgetSeriesList);
                    logger.debug("Checking the match for {}", metricTemplate);
                    List<String> matches = getMatchingMetrics(metrics, metricTemplate);
                    if (matches != null && !matches.isEmpty()) {
                        for (String match : matches) {
                            Node widgetClone = widget.cloneNode(true);
                            Node cloneMetricNameNode = Xml.getFirstDescendant(widgetClone, "metric-name");
                            cloneMetricNameNode.setTextContent(match);
                            addMap.put(widgetClone, widgetSeriesList);
                            ++seriesNameCount;
                            setSeriesName(seriesNameCount, widgetClone);
                            Node firstNode = Xml.getFirstChild(widgetClone, "widget-series-data");
                            setApplicationName(firstNode, controllerInfo.getApplicationName());
                        }
                    } else {
                        logger.error("No Match found for {}", metricTemplate);
                    }
                } else {
                    removeMap.put(widget, widget.getParentNode());
                }
            }
        }
        for (Map.Entry<Node, Node> entry : removeMap.entrySet()) {
            entry.getValue().removeChild(entry.getKey());
        }
        for (Map.Entry<Node, Node> entry : addMap.entrySet()) {
            entry.getValue().appendChild(entry.getKey());
        }
        if (!addMap.isEmpty()) {
            persistDashboard(dashboardName, xml);
        } else {
            logger.error("Dashboard cannot be created, all of the metric path resolution failed. Check previous logs for details");
        }
    }

    protected InputStream getDashboardTemplate() {
        String template = (String) dashboardConfig.get("templateFile");
        if (template != null) {
            File file = PathResolver.getFile(template, AManagedMonitor.class);
            if (file != null && file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                }
            } else {
                logger.error("Cannot resolve template file {}", file != null ? file.getAbsolutePath() : template);
            }
        }
        return getClass().getResourceAsStream("/dashboard/custom-dashboard-template.xml");
    }

    protected void persistDashboard(String dashboardName, Xml xml) {
        if (getBoolean(dashboardConfig, "uploadDashboard")) {
            Map<String, ? super Object> argsMap = getArgsMap();
            CloseableHttpClient httpClient = getHttpClient(argsMap);

            try {
                dashboardUploader.uploadDashboard(httpClient,dashboardName, ".xml", xml.toString(), "text/xml", argsMap,
                        getBoolean(dashboardConfig, "overwriteDashboard"));
            } catch (ApiException e) {
                logger.error("Failed to upload dashboard", e);
            }
        }
        writeDashboardToFile(dashboardName, xml);
    }
    private CloseableHttpClient getHttpClient(Map<String, ? super Object> argsMap) {

        setProxyIfApplicable(argsMap);
        CloseableHttpClient client = null;

        try {
            client = Http4ClientBuilder.getBuilder(argsMap).build();
            return client;
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void setProxyIfApplicable(Map<String, ? super Object> argsMap) {
        String proxyHost = System.getProperty("appdynamics.http.proxyHost");
        String proxyPort = System.getProperty("appdynamics.http.proxyPort");
        if (StringUtils.hasText(proxyHost) && StringUtils.hasText(proxyPort)) {
            Map<String, ? super Object> proxyMap = new HashMap<>();
            proxyMap.put(TaskInputArgs.HOST, proxyHost);
            proxyMap.put(TaskInputArgs.PORT, proxyPort);
            argsMap.put("proxy", proxyMap);
            logger.debug("Using the proxy {}:{} to upload the dashboard", proxyHost, proxyPort);
        } else {
            logger.debug("Not using proxy for dashboard upload appdynamics.http.proxyHost={} and appdynamics.http.proxyPort={}"
                    , proxyHost, proxyPort);
        }
    }
    private void writeDashboardToFile(String dashboardName, Xml xml) {
        File file = PathResolver.resolveDirectory(AManagedMonitor.class);
        File dir = new File(file, "logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(new File(dir, dashboardName + ".xml"));
            writer.write(xml.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private boolean getBoolean(Map map, String key) {
        Object o = map.get(key);
        if (o instanceof Boolean) {
            return (Boolean) o;

        }
        return false;
    }

    private void setApplicationName(Node widgetSeriesData, String applicationName) {
        addAttribute(widgetSeriesData, "application-name", applicationName);
    }

    private void setSeriesName(int seriesNameCount, Node widgetClone) {
        String nodeName = "name";
        String nodeValue = "Series " + (seriesNameCount);
        addAttribute(widgetClone, nodeName, nodeValue);
    }

    private void addAttribute(Node node, String attrName, String attrValue) {
        NamedNodeMap attributes = node.getAttributes();
        boolean attrAdded = false;
        if (attributes != null) {
            for (int j = 0; j < attributes.getLength(); j++) {
                Node item = attributes.item(j);
                if (item.getNodeName().equals(attrName)) {
                    item.setNodeValue(attrValue);
                    attrAdded = true;
                }
            }
        }
        if (!attrAdded) {
            Attr name = node.getOwnerDocument().createAttribute(attrName);
            name.setNodeValue(attrValue);
            node.appendChild(name);
        }
    }

    protected String replacePrefixAndInstanceName(String metricTemplate, String ctrlMetricPrefix, String instanceName) {
        metricTemplate = metricTemplate.replace("${METRIC_PREFIX}", ctrlMetricPrefix);
        if (StringUtils.hasText(instanceName)) {
            metricTemplate = metricTemplate.replace("${INSTANCE_NAME}", instanceName);
        } else {
            if (metricTemplate.contains("${INSTANCE_NAME}")) {
                metricTemplate = metricTemplate.replace("|${INSTANCE_NAME}|", "|");
            }
        }
        return metricTemplate;
    }

    public List<String> getMatchingMetrics(Collection<String> metrics, String template) {
        int segments = StringUtils.countMatches(template, "|");
        List<String> matches = new ArrayList<String>();
        for (String metric : metrics) {
            if (template.equals(metric)) {
                matches.add(metric);
            } else if (StringUtils.countMatches(metric, "|") == segments) {
                String replacedTemplate = template;
                int itemStart;
                while ((itemStart = replacedTemplate.indexOf("${ITEM")) != -1) {
                    String substring = replacedTemplate.substring(0, itemStart);
                    if (metric.startsWith(substring)) {
                        String replacement = extractSegment(metric, itemStart);
                        replacedTemplate = replaceSegment(replacedTemplate, itemStart, replacement);
                    } else {
                        break;
                    }
                }
                if (metric.equals(replacedTemplate)) {
                    matches.add(metric);
                }
            } else {
                //logger.debug("No match metric = {}, template = {}", metric, template);
            }
        }
        return matches;
    }

    private String replaceSegment(String template, int itemStart, String replacement) {
        int endIndex = template.indexOf("|", itemStart);
        if (endIndex != -1) {
            StringBuilder sb = new StringBuilder();
            sb.append(template.substring(0, itemStart));
            sb.append(replacement);
            sb.append(template.substring(endIndex));
            return sb.toString();
        }
        return null;
    }

    private String extractSegment(String metric, int itemStart) {
        int endIndex = metric.indexOf("|", itemStart);
        if (endIndex != -1) {
            return metric.substring(itemStart, endIndex);
        }
        return null;
    }

}
