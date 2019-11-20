package com.appdynamics.extensions.discovery.k8s;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(PowerMockRunner.class)
@PrepareForTest({KubernetesConnectionBuilder.class, KubernetesDiscoveryService.KubernetesDiscoveryBuilder.class})
public class KubernetesDiscoveryServiceTest {


    @Mock
    private KubernetesClient kubernetesClientMock;

    @Mock
    private KubernetesConnectionBuilder kubernetesConnectionBuilderMock;

    @Mock
    private MixedOperation mixedOperationPodsMock;

    @Mock
    private FilterWatchListDeletable filterWatchListDeletableMock;

    @Mock
    private PodList podListMock;

    private HashMap<String, String> labels = Maps.newHashMap();


    @Before
    public void setup() throws Exception {

        PowerMockito.whenNew(KubernetesConnectionBuilder.class).withAnyArguments().thenReturn(kubernetesConnectionBuilderMock);

        Mockito.when(kubernetesConnectionBuilderMock.withNamespace(Matchers.anyString())).thenReturn(kubernetesConnectionBuilderMock);

        Mockito.when(kubernetesConnectionBuilderMock.build()).thenReturn(kubernetesClientMock);

        labels.put("name", "nginx-pod");
    }

    @Test
    public void testDiscovery() {

        setupPodWithSingleContainerPort();

        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withLabel("name", "nginx-pod").build();

        List<PodAddress> discover = kubernetesDiscoveryService.discover();

        Assert.assertNotNull("Pod endpoint should not be null", discover);
        Assert.assertEquals("Should have 1 Pod endpoint", discover.size(), 1);

        PodAddress privateAddress = discover.get(0);

        Assert.assertNotNull("Pod public address should not be null", privateAddress);

        Assert.assertEquals("Pod's public IP should be the node ip", privateAddress.getHost(), "podIp1");
        Assert.assertEquals("Pod's public port should be the NodePort port", privateAddress.getPort(), Integer.valueOf(80));
    }

    @Test
    public void testDiscoveryWithMultipleContainerPorts() {

        HashMap<String, Integer> containerPorts = Maps.newHashMap();
        containerPorts.put("httpPort", 80);
        containerPorts.put("jmxPort", 9080);

        setupPodWithMultipleContainerPorts(containerPorts);

        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withLabel("name", "nginx-pod")
                .withContainerPortName("httpPort").build();

        List<PodAddress> discover = kubernetesDiscoveryService.discover();

        Assert.assertNotNull("Pod endpoint should not be null", discover);
        Assert.assertEquals("Should have 1 Pod endpoint", discover.size(), 1);

        PodAddress privateAddress = discover.get(0);

        Assert.assertNotNull("Pod public address should not be null", privateAddress);

        Assert.assertEquals("Pod's public IP should be the node ip", privateAddress.getHost(), "podIp1");
        Assert.assertEquals("Pod's public port should be the NodePort port", privateAddress.getPort(), Integer.valueOf(80));
    }

    @Test(expected = KubernetesClientException.class)
    public void testDiscoveryWithMultipleContainerPortsAndWithNoConfig() {

        HashMap<String, Integer> containerPorts = Maps.newHashMap();
        containerPorts.put("httpPort", 80);
        containerPorts.put("jmxPort", 9080);

        setupPodWithMultipleContainerPorts(containerPorts);

        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withLabel("name", "nginx-pod").build();

        kubernetesDiscoveryService.discover();
    }

    @Test(expected = KubernetesClientException.class)
    public void testDiscoveryWithMultipleContainerPortsAndWithNoPort() {

        HashMap<String, Integer> containerPorts = Maps.newHashMap();
        containerPorts.put("jmxPort", 9080);
        containerPorts.put("rtpPort", 9080);

        setupPodWithMultipleContainerPorts(containerPorts);

        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withContainerPortName("httpPort").withLabel("name", "nginx-pod").build();

        kubernetesDiscoveryService.discover();
    }

    private void setupPodWithSingleContainerPort() {
        ContainerPort containerPort = new ContainerPortBuilder().withContainerPort(80).build();
        Container container = new ContainerBuilder().withImage("nginx").withPorts(containerPort).build();
        PodSpec podSpec = new PodSpecBuilder().withContainers(container).withNodeName("Node1").build();

        PodStatus podStatus1 = new PodStatusBuilder().withPodIP("podIp1").withHostIP("hostIp1").withPhase("Running").withContainerStatuses(new ContainerStatusBuilder().withReady(true).build()).build();
        Pod pod1 = new PodBuilder().withNewMetadata().withName("pod1").withLabels(labels).endMetadata().withSpec(podSpec).withStatus(podStatus1).build();

        PodStatus podStatus2 = new PodStatusBuilder().withPodIP("podIp1").withHostIP("hostIp1").withPhase("Terminating").withContainerStatuses(new ContainerStatusBuilder().withReady(true).build()).build();
        Pod pod2 = new PodBuilder().withNewMetadata().withName("pod1").withLabels(labels).endMetadata().withSpec(podSpec).withStatus(podStatus2).build();

        Mockito.when(kubernetesClientMock.pods()).thenReturn(mixedOperationPodsMock);
        Mockito.when(mixedOperationPodsMock.withLabels(labels)).thenReturn(filterWatchListDeletableMock);
        Mockito.when(filterWatchListDeletableMock.list()).thenReturn(podListMock);

        Mockito.when(kubernetesClientMock.pods().withLabels(labels).list().getItems()).thenReturn(Lists.newArrayList(pod1, pod2));
    }

    private void setupPodWithMultipleContainerPorts(Map<String, Integer> containerPorts) {

        List<ContainerPort> podContainerPorts = new ArrayList<>();
        for (Map.Entry<String, Integer> containerPort : containerPorts.entrySet()) {
            ContainerPort podContainerPort = new ContainerPortBuilder().withName(containerPort.getKey()).withContainerPort(containerPort.getValue()).build();
            podContainerPorts.add(podContainerPort);
        }
        Container container = new ContainerBuilder().withImage("nginx").withPorts(podContainerPorts).build();
        PodSpec podSpec = new PodSpecBuilder().withContainers(container).withNodeName("Node1").build();

        PodStatus podStatus1 = new PodStatusBuilder().withPodIP("podIp1").withHostIP("hostIp1").withPhase("Running").withContainerStatuses(new ContainerStatusBuilder().withReady(true).build()).build();
        Pod pod1 = new PodBuilder().withNewMetadata().withName("pod1").withLabels(labels).endMetadata().withSpec(podSpec).withStatus(podStatus1).build();

        PodStatus podStatus2 = new PodStatusBuilder().withPodIP("podIp1").withHostIP("hostIp1").withPhase("Terminating").withContainerStatuses(new ContainerStatusBuilder().withReady(true).build()).build();
        Pod pod2 = new PodBuilder().withNewMetadata().withName("pod1").withLabels(labels).endMetadata().withSpec(podSpec).withStatus(podStatus2).build();

        Mockito.when(kubernetesClientMock.pods()).thenReturn(mixedOperationPodsMock);
        Mockito.when(mixedOperationPodsMock.withLabels(labels)).thenReturn(filterWatchListDeletableMock);
        Mockito.when(filterWatchListDeletableMock.list()).thenReturn(podListMock);

        Mockito.when(kubernetesClientMock.pods().withLabels(labels).list().getItems()).thenReturn(Lists.newArrayList(pod1, pod2));
    }

    @Test
    public void testCloseClient() {
        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withLabel("name", "nginx-pod").build();
        kubernetesDiscoveryService.closeClient();

        Mockito.verify(kubernetesClientMock, Mockito.times(1)).close();
    }
}