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
import com.appdynamics.extensions.xml.Xml;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.appdynamics.extensions.dashboard.DashboardConstants;
public class CustomDashboardGeneratorTest {

    private CustomDashboardGenerator customDashboardGenerator;

    private File file = Mockito.mock(File.class);

    @Before
    public void before() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File confDir = new File(tmpDir, "conf");
        if (!confDir.exists()) {
            confDir.mkdirs();
        }
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/dashboard/test-controller-info.xml")
                , new File(confDir, "controller-info.xml"));
        System.setProperty("singularity.install.dir", tmpDir.getAbsolutePath());
        Set<String> instances = new HashSet<String>(Arrays.asList("Server1"));
        String metricPrefix = "Server|Component:Tier1|Custom Metrics|Docker|||";
        Map config = new HashMap();
        config.put("namePrefix", "Docker");
        config.put("enabled", true);
        config.put("uploadDashboard", true);
        customDashboardGenerator = Mockito.spy(new CustomDashboardGenerator(instances, metricPrefix, config, file));
        Mockito.doReturn(getClass()
                .getResourceAsStream("/dashboard/test-custom-dashboard-template.xml")).when(customDashboardGenerator)
                .getDashboardTemplate();
    }

    @Test
    public void getMatchingMetricsWithOneItem() {
        List<String> metrics = new ArrayList<String>();
        metrics.add("Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|/rabbitmq_rabbit1_1|Network|Receive|MB Per Minute");
        metrics.add("Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|/rabbitmq_rabbit2_1|Network|Receive|MB Per Minute");
        metrics.add("Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|/rabbitmq_rabbit2_1|Network|Receive|MB Per Minute");

        String template = "Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|${ITEM0}|Network|Receive|MB Per Minute";
        Assert.assertEquals(3, customDashboardGenerator.getMatchingMetrics(metrics, template).size());
    }

    @Test
    public void getMatchingMetricsWithTwoItems() {
        List<String> metrics = new ArrayList<String>();
        metrics.add("Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|/rabbitmq_rabbit1_1|Network_1|Receive|MB Per Minute");
        metrics.add("Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|/rabbitmq_rabbit2_1|Network_2|Receive|MB Per Minute");
        metrics.add("Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|/rabbitmq_rabbit2_1|Network_3|Receive|MB Per Minute");

        String template = "Application Infrastructure Performance|Tier1|Custom Metrics|Docker|Unix1|${ITEM0}|${ITEM1}|Receive|MB Per Minute";
        Assert.assertEquals(3, customDashboardGenerator.getMatchingMetrics(metrics, template).size());
    }

    @Test
    public void createDashboards() {
        List<String> metrics = new ArrayList<String>();
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Receive|Dropped");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Receive|MB");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Receive|MB Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Receive|Errors");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Receive|Packets");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Transmit|Dropped");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Transmit|MB");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Transmit|MB Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Transmit|Errors");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Transmit|Packets");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Receive|Errors Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Network|Transmit|Errors Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Memory|Max Usage (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Memory|Current (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Memory|Limit (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Memory|Fail Count");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|CPU|System (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|CPU|User Mode (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|CPU|Total (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|CPU|Total %");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|CPU|Kernel (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit3_1|Memory|Current %");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Receive|Dropped");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Receive|MB");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Receive|MB Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Receive|Errors");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Receive|Packets");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Receive|Errors Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Transmit|Dropped");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Transmit|MB");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Transmit|MB Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Transmit|Errors");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Transmit|Packets");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Network|Transmit|Errors Per Minute");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Memory|Max Usage (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Memory|Current (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Memory|Limit (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|Memory|Fail Count");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|CPU|System (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|CPU|User Mode (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|CPU|Total (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|/rabbitmq_rabbit2_1|CPU|Kernel (Ticks)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|Summary|Running Container Count");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|Summary|Container Count");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|Summary|Image Count");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|Summary|Total Memory (MB)");
        metrics.add("Server|Component:Tier1|Custom Metrics|Docker|Server1|Summary|MemoryLimit");


        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put(TaskInputArgs.HOST, "192.168.1.134");
        argsMap.put(TaskInputArgs.PORT, "8090");
        argsMap.put(TaskInputArgs.USE_SSL, "false");
        argsMap.put(TaskInputArgs.USER, "singularity-agent@customer1");
        argsMap.put(TaskInputArgs.PASSWORD, "SJ5b2m7d1$354");
        Mockito.doReturn(argsMap).when(customDashboardGenerator).getArgsMap();
        Mockito.doReturn(true).when(customDashboardGenerator).isResolved();
        final AtomicInteger count = new AtomicInteger();
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock inv) throws Throwable {
                Xml xml = (Xml) inv.getArguments()[1];
                List<Node> descendants = Xml.getDescendants(xml.getSource(), "metric-name");
                Assert.assertEquals(descendants.size(), 14);
                count.incrementAndGet();
                return null;
            }
        }).when(customDashboardGenerator).persistDashboard(Mockito.anyString(), Mockito.any(Xml.class));
        customDashboardGenerator.createDashboards(metrics);
        Assert.assertEquals("The method persistDashboard was not invoked", 1, count.get());
    }

    @Test
    public void buildMetricPrefix() {
        AgentEnvironmentResolver resolver = Mockito.mock(AgentEnvironmentResolver.class);
        customDashboardGenerator.setAgentEnvResolver(resolver);
        Mockito.doReturn("Tier1").when(resolver).getTierName();

        String metricPrefix = "Server|Component:Tier1|Custom Metrics|Docker";
        String ctrlMetricPrefix = customDashboardGenerator.buildMetricPrefix(metricPrefix).toString();
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Custom Metrics|Docker", ctrlMetricPrefix);

        metricPrefix = "Server|Component:Tier1|Custom Metrics|||";
        ctrlMetricPrefix = customDashboardGenerator.buildMetricPrefix(metricPrefix).toString();
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Custom Metrics", ctrlMetricPrefix);

        metricPrefix = "Server|Component:Tier1|";
        ctrlMetricPrefix = customDashboardGenerator.buildMetricPrefix(metricPrefix).toString();
        Assert.assertEquals("Application Infrastructure Performance|Tier1", ctrlMetricPrefix);

        metricPrefix = "Server|Component:Tier1";
        ctrlMetricPrefix = customDashboardGenerator.buildMetricPrefix(metricPrefix).toString();
        Assert.assertEquals("Application Infrastructure Performance|Tier1", ctrlMetricPrefix);

        metricPrefix = "Custom Metrics|Docker|||";
        ctrlMetricPrefix = customDashboardGenerator.buildMetricPrefix(metricPrefix).toString();
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Custom Metrics|Docker", ctrlMetricPrefix);
    }

    @Test
    public void replacePrefixAndInstanceName() {
        String metricPrefix = "Application Infrastructure Performance|Tier1";
        String template = "${METRIC_PREFIX}|${INSTANCE_NAME}|Container";
        String instanceName = "Server1";
        String prefix = customDashboardGenerator.replacePrefixAndInstanceName(template, metricPrefix, instanceName);
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Server1|Container", prefix);

        instanceName = "";
        prefix = customDashboardGenerator.replacePrefixAndInstanceName(template, metricPrefix, instanceName);
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Container", prefix);

        instanceName = null;
        prefix = customDashboardGenerator.replacePrefixAndInstanceName(template, metricPrefix, instanceName);
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Container", prefix);

        instanceName = "Server1";
        template = "${METRIC_PREFIX}|Container";
        prefix = customDashboardGenerator.replacePrefixAndInstanceName(template, metricPrefix, instanceName);
        Assert.assertEquals("Application Infrastructure Performance|Tier1|Container", prefix);
    }

//    @Test
//    public void loginTest(){
//        Map<String, ?> config = YmlReader.readFromFile(new File("/Users/abey.tom/cstools/loganalyzer/zendesk/58694/March-29-2016_12.26.19/config.yml"));
//        Map dashboard = (Map) config.get("customDashboard");
//        String metricPrefix = "Application Infrastructure Performance|Tier1";
//        HashSet<String> names = new HashSet<String>();
//        names.add("server");
//        CustomDashboardGenerator docker = new CustomDashboardGenerator(names, "Docker", dashboard);
//        docker.persistDashboard("Test",new Xml("<xml/>"));
//
//    }


    @Test
    public void replaceDefaultValuesInTheNormalDashboard() throws Exception{
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("dashboardName", "Dashboard Test");
        dashboardConfig.put("enabled", true);
        dashboardConfig.put("uploadDashboard", true);

        Map controllerConfig = new HashMap();
        controllerConfig.put("host", "192.168.11.02");
        controllerConfig.put("port", "1100");
        String metricPrefix = "Custom Metrics|Extension|";
        CustomDashboardUploader uploader = Mockito.mock(CustomDashboardUploader.class);

        CustomDashboardGenerator customDashboardGen;

        customDashboardGen = new CustomDashboardGenerator(file, dashboardConfig, controllerConfig,metricPrefix, uploader );

        AgentEnvironmentResolver agentEnvironmentResolver = Mockito.mock(AgentEnvironmentResolver.class);
        Mockito.when(agentEnvironmentResolver.getApplicationName()).thenReturn("Application");
        Mockito.when(agentEnvironmentResolver.getTierName()).thenReturn("Tier");
        Mockito.when(agentEnvironmentResolver.getNodeName()).thenReturn("Node");
        Mockito.when(agentEnvironmentResolver.getControllerHostName()).thenReturn("ControllerHost");
        Mockito.when(agentEnvironmentResolver.getMachinePath()).thenReturn("MachinePath");
        customDashboardGen.setAgentEnvResolver(agentEnvironmentResolver);

        String dashboardString = FileUtils.readFileToString(new File("src/test/resources/dashboard/normalDashboard.json"));
        String updatedDashboardString = customDashboardGen.setDefaultDashboardInfo(dashboardString);

        Assert.assertFalse(dashboardString.equals(updatedDashboardString));

        if(dashboardString.contains(DashboardConstants.REPLACE_APPLICATION_NAME)){
            Assert.assertTrue(updatedDashboardString.contains("Application"));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_SIM_APPLICATION_NAME)){
            Assert.assertTrue(updatedDashboardString.contains(DashboardConstants.SIM_APPLICATION_NAME));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_MACHINE_PATH)){
            Assert.assertTrue(updatedDashboardString.contains("MachinePath"));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_TIER_NAME)){
            Assert.assertTrue(updatedDashboardString.contains("Tier"));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_NODE_NAME)){
            Assert.assertTrue(updatedDashboardString.contains("Node"));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)){
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }

    }

    @Test
    public void replaceDefaultValuesInTheSIMDashboard() throws Exception{
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("dashboardName", "Dashboard Test");
        dashboardConfig.put("enabled", true);
        dashboardConfig.put("uploadDashboard", true);

        Map controllerConfig = new HashMap();
        controllerConfig.put("host", "192.168.11.02");
        controllerConfig.put("port", "1100");
        String metricPrefix = "Custom Metrics|Extension|";
        CustomDashboardUploader uploader = Mockito.mock(CustomDashboardUploader.class);

        CustomDashboardGenerator customDashboardGen;
        customDashboardGen = new CustomDashboardGenerator(file, dashboardConfig, controllerConfig,metricPrefix, uploader );

        customDashboardGen.setAgentEnvResolver(getAgentEnvResolverWithSim());

        String dashboardString = FileUtils.readFileToString(new File("src/test/resources/dashboard/simDashboard.json"));
        String updatedDashboardString = customDashboardGen.setDefaultDashboardInfo(dashboardString);

        Assert.assertFalse(dashboardString.equals(updatedDashboardString));

        if(dashboardString.contains(DashboardConstants.REPLACE_SIM_APPLICATION_NAME)){
            Assert.assertTrue(updatedDashboardString.contains(DashboardConstants.SIM_APPLICATION_NAME));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_MACHINE_PATH)){
            Assert.assertTrue(updatedDashboardString.contains("Root"));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_HOST_NAME)){
            Assert.assertTrue(updatedDashboardString.contains("ControllerHostName"));
        }
        if(dashboardString.contains(DashboardConstants.REPLACE_METRIC_PREFIX)){
            Assert.assertTrue(updatedDashboardString.contains(metricPrefix));
        }

    }

    @Test
    public void testCreateDashboard(){
        Map dashboardConfig = new HashMap();
        dashboardConfig.put("dashboardName", "Dashboard Test");
        dashboardConfig.put("enabled", true);
        dashboardConfig.put("uploadDashboard", true);
        dashboardConfig.put("pathToNormalDashboard","src/test/resources/dashboard/normalDashboard.json");

        Map controllerConfig = new HashMap();
        controllerConfig.put("host", "192.168.11.02");
        controllerConfig.put("port", "1100");
        String metricPrefix = "Custom Metrics|Extension|";

        CustomDashboardGenerator customDashboardGen;
        CustomDashboardUploader uploader = Mockito.mock(CustomDashboardUploader.class);

        customDashboardGen = new CustomDashboardGenerator(file, dashboardConfig, controllerConfig,metricPrefix, uploader );

        customDashboardGen.setAgentEnvResolver(getAgentEnvResolverWithoutSim());

        customDashboardGen.createDashboard();
        try{
            Mockito.verify(uploader, Mockito.times(1)).uploadDashboard( Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyMap(),Mockito.anyBoolean());

        } catch (ApiException e){
            Assert.assertFalse(true);
        }
    }

    public AgentEnvironmentResolver getAgentEnvResolverWithoutSim(){
        AgentEnvironmentResolver agentEnvironmentResolver = Mockito.mock(AgentEnvironmentResolver.class);
        Mockito.when(agentEnvironmentResolver.getApplicationName()).thenReturn("Application");
        Mockito.when(agentEnvironmentResolver.getTierName()).thenReturn("Tier");
        Mockito.when(agentEnvironmentResolver.getNodeName()).thenReturn("Node");
        Mockito.when(agentEnvironmentResolver.getControllerHostName()).thenReturn("ControllerHostName");
        Mockito.when(agentEnvironmentResolver.getControllerPort()).thenReturn(9090);
        Mockito.when(agentEnvironmentResolver.getMachinePath()).thenReturn("MachinePath");
        Mockito.when(agentEnvironmentResolver.isControllerUseSSL()).thenReturn(false);
        Mockito.when(agentEnvironmentResolver.getAccountName()).thenReturn("Account");
        Mockito.when(agentEnvironmentResolver.getUsername()).thenReturn("UserName");
        Mockito.when(agentEnvironmentResolver.getPassword()).thenReturn("PassWord");
        Mockito.when(agentEnvironmentResolver.getSimEnabled()).thenReturn(false);
        Mockito.when(agentEnvironmentResolver.isResolved()).thenReturn(true);

        return agentEnvironmentResolver;
    }

    public AgentEnvironmentResolver getAgentEnvResolverWithSim(){
        AgentEnvironmentResolver agentEnvironmentResolver = Mockito.mock(AgentEnvironmentResolver.class);
        Mockito.when(agentEnvironmentResolver.getControllerHostName()).thenReturn("ControllerHostName");
        Mockito.when(agentEnvironmentResolver.getControllerPort()).thenReturn(9090);
        Mockito.when(agentEnvironmentResolver.getMachinePath()).thenReturn("MachinePath");
        Mockito.when(agentEnvironmentResolver.isControllerUseSSL()).thenReturn(false);
        Mockito.when(agentEnvironmentResolver.getAccountName()).thenReturn("Account");
        Mockito.when(agentEnvironmentResolver.getUsername()).thenReturn("UserName");
        Mockito.when(agentEnvironmentResolver.getPassword()).thenReturn("PassWord");
        Mockito.when(agentEnvironmentResolver.getSimEnabled()).thenReturn(true);
        Mockito.when(agentEnvironmentResolver.isResolved()).thenReturn(true);

        return agentEnvironmentResolver;
    }
}