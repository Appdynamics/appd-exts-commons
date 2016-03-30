package com.appdynamics.extensions.dashboard;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.xml.Xml;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomDashboardGeneratorTest {

    private CustomDashboardGenerator customDashboardGenerator;

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
        customDashboardGenerator = Mockito.spy(new CustomDashboardGenerator(instances, metricPrefix, config));
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
        Assert.assertEquals("Application Infrastructure Performance|tier|Custom Metrics|Docker", ctrlMetricPrefix);
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

}