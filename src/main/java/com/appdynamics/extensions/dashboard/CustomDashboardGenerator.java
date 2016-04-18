package com.appdynamics.extensions.dashboard;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.StringUtils;
import com.appdynamics.extensions.xml.Xml;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;

/**
 * Created by abey.tom on 4/10/15.
 */
public class CustomDashboardGenerator {
    public static final Logger logger = LoggerFactory.getLogger(CustomDashboardGenerator.class);
    public static final String TIER_METRIC_PREFIX = "Server|Component:";
    private Set<String> instanceNames;
    private String metricPrefix;
    private Map dashboardConfig;
    private AgentEnvironmentResolver agentEnvResolver;
    protected CustomDashboardUploader dashboardUploader;

    public CustomDashboardGenerator(Set<String> instanceNames, String metricPrefix, Map dashboardConfig) {
        if (dashboardConfig == null) {
            logger.info("Custom Dashboard config is null");
            return;
        }
        Boolean enabled = (Boolean) dashboardConfig.get("enabled");
        if (enabled == null || !enabled) {
            logger.info("Custom Dashboard creation is not enabled");
            return;
        }
        this.instanceNames = instanceNames;
        this.metricPrefix = StringUtils.trim(metricPrefix, "|");
        this.dashboardConfig = dashboardConfig;
        this.agentEnvResolver = new AgentEnvironmentResolver(dashboardConfig);
        this.dashboardUploader = new CustomDashboardUploader();
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

    protected boolean isResolved() {
        return agentEnvResolver != null && agentEnvResolver.isResolved();
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
        String tierName = agentEnvResolver.getTierName();
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
                            setApplicationName(firstNode, agentEnvResolver.getApplicationName());
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

    protected void persistDashboard(String dashboardName, Xml xml) {
        if (getBoolean(dashboardConfig, "uploadDashboard")) {
            Map<String, String> argsMap = getArgsMap();
            dashboardUploader.uploadDashboard(dashboardName, xml, argsMap,
                    getBoolean(dashboardConfig, "overwriteDashboard"));
        }
        writeDashboardToFile(dashboardName, xml);
    }


    protected Map<String, String> getArgsMap() {
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put(TaskInputArgs.HOST, agentEnvResolver.getControllerHostName());
        argsMap.put(TaskInputArgs.PORT, String.valueOf(agentEnvResolver.getControllerPort()));
        argsMap.put(TaskInputArgs.USE_SSL, String.valueOf(agentEnvResolver.isControllerUseSSL()));
        argsMap.put(TaskInputArgs.USER, getUserName());
        argsMap.put(TaskInputArgs.PASSWORD, agentEnvResolver.getPassword());
        argsMap.put(TaskInputArgs.SSL_PROTOCOL, "TLSv1.2");
        Object sslCertCheckEnabled = dashboardConfig.get("sslCertCheckEnabled");
        if (sslCertCheckEnabled != null) {
            argsMap.put("sslCertCheckEnabled", sslCertCheckEnabled.toString());
        } else {
            argsMap.put("sslCertCheckEnabled", "true");
        }
        return argsMap;
    }

    private String getUserName() {
        String accountName = agentEnvResolver.getAccountName();
        String username = agentEnvResolver.getUsername();
        if (accountName != null && username != null) {
            return username + "@" + accountName;
        }
        return "";
    }

    private void writeDashboardToFile(String dashboardName, Xml xml) {
        File file = PathResolver.resolveDirectory(AManagedMonitor.class);
        File dir = new File(file, "logs");
        if(!dir.exists()){
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

    protected void setAgentEnvResolver(AgentEnvironmentResolver agentEnvResolver) {
        this.agentEnvResolver = agentEnvResolver;
    }
}
