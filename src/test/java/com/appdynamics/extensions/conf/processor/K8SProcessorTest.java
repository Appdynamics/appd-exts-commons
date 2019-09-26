package com.appdynamics.extensions.conf.processor;

import com.appdynamics.extensions.discovery.k8s.KubernetesDiscoveryService;
import com.appdynamics.extensions.discovery.k8s.PodAddress;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({K8SProcessor.class})
public class K8SProcessorTest {

    private static final String CONFIG_FILE = "src/test/resources/k8s/config.yml";

    @Mock
    private KubernetesDiscoveryService kubernetesDiscoveryService;
    @Mock
    private KubernetesDiscoveryService.KubernetesDiscoveryBuilder kubernetesDiscoveryBuilder;


    @Test
    public void testProcessForSingleServer() throws Exception {

        PowerMockito.whenNew(KubernetesDiscoveryService.KubernetesDiscoveryBuilder.class).withNoArguments().thenReturn(kubernetesDiscoveryBuilder);

        Mockito.when(kubernetesDiscoveryBuilder.withNamespace(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withContainerImage(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withLabels(Matchers.anyMap())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.build()).thenReturn(kubernetesDiscoveryService);

        Mockito.when(kubernetesDiscoveryService.discover()).thenReturn(Lists.newArrayList(new PodAddress("ip1", 80)));

        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(new File(CONFIG_FILE));

        Map<String, ?> configAfterProcess = K8SProcessor.process(rootElem);

        Assert.assertNotNull("Config after process should not be null", configAfterProcess);

        List<Map<String, ?>> serversList = (List<Map<String, ?>>) configAfterProcess.get("servers");

        Assert.assertNotNull("Servers after process should not be null", serversList);
        Assert.assertEquals("Servers size should be 1", 1, serversList.size());

        Map<String, ?> serverConfig = serversList.get(0);

        String serverURI = (String) serverConfig.get("uri");
        Assert.assertEquals("Servers size should be 1", "http://ip1:80/nginx_status", serverURI);

        Mockito.verify(kubernetesDiscoveryService, Mockito.times(1)).closeClient();

    }

    @Test
    public void testProcessForMultipleServers() throws Exception {

        PowerMockito.whenNew(KubernetesDiscoveryService.KubernetesDiscoveryBuilder.class).withNoArguments().thenReturn(kubernetesDiscoveryBuilder);

        Mockito.when(kubernetesDiscoveryBuilder.withNamespace(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withContainerImage(Matchers.anyString())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.withLabels(Matchers.anyMap())).thenReturn(kubernetesDiscoveryBuilder);
        Mockito.when(kubernetesDiscoveryBuilder.build()).thenReturn(kubernetesDiscoveryService);

        Mockito.when(kubernetesDiscoveryService.discover()).thenReturn(Lists.newArrayList(new PodAddress("ip1", 80), new PodAddress("ip2", 80)));

        Map<String, ?> rootElem = YmlReader.readFromFileAsMap(new File(CONFIG_FILE));

        Map<String, ?> configAfterProcess = K8SProcessor.process(rootElem);

        Assert.assertNotNull("Config after process should not be null", configAfterProcess);

        List<Map<String, ?>> serversList = (List<Map<String, ?>>) configAfterProcess.get("servers");

        Assert.assertNotNull("Servers after process should not be null", serversList);
        Assert.assertEquals("Servers size should be 2", 2, serversList.size());

        Map<String, ?> serverConfig = serversList.get(0);

        String serverURI = (String) serverConfig.get("uri");
        Assert.assertEquals("Invalid server url", "http://ip1:80/nginx_status", serverURI);

        Map<String, ?> serverConfig1 = serversList.get(1);

        String serverURI1 = (String) serverConfig1.get("uri");
        Assert.assertEquals("Invalid server url", "http://ip2:80/nginx_status", serverURI1);

        Mockito.verify(kubernetesDiscoveryService, Mockito.times(1)).closeClient();
    }
}