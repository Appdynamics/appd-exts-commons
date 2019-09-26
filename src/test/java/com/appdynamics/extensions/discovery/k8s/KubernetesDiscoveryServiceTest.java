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

import java.util.HashMap;
import java.util.List;


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

        setupPod();
    }

    @Test
    public void testDiscovery() {
        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withLabel("name", "nginx-pod").build();

        List<PodAddress> discover = kubernetesDiscoveryService.discover();

        Assert.assertNotNull("Pod endpoint should not be null", discover);
        Assert.assertEquals("Should have 1 Pod endpoint", discover.size(), 1);

        PodAddress privateAddress = discover.get(0);

        Assert.assertNotNull("Pod public address should not be null", privateAddress);

        Assert.assertEquals("Pod's public IP should be the node ip", privateAddress.getHost(), "podIp1");
        Assert.assertEquals("Pod's public port should be the NodePort port", privateAddress.getPort(), Integer.valueOf(80));
    }

    private void setupPod() {
        ContainerPort containerPort = new ContainerPortBuilder().withContainerPort(80).build();
        Container container = new ContainerBuilder().withImage("nginx").withPorts(containerPort).build();
        PodSpec podSpec = new PodSpecBuilder().withContainers(container).withNodeName("Node1").build();
        PodStatus podStatus = new PodStatusBuilder().withPodIP("podIp1").withHostIP("hostIp1").withContainerStatuses(new ContainerStatusBuilder().withReady(true).build()).build();

        Pod pod = new PodBuilder().withNewMetadata().withName("pod1").withLabels(labels).endMetadata().withSpec(podSpec).withStatus(podStatus).build();

        Mockito.when(kubernetesClientMock.pods()).thenReturn(mixedOperationPodsMock);
        Mockito.when(mixedOperationPodsMock.withLabels(labels)).thenReturn(filterWatchListDeletableMock);
        Mockito.when(filterWatchListDeletableMock.list()).thenReturn(podListMock);

        Mockito.when(kubernetesClientMock.pods().withLabels(labels).list().getItems()).thenReturn(Lists.newArrayList(pod));
    }

    @Test
    public void testCloseClient() {
        KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withContainerImage("nginx").withLabel("name", "nginx-pod").build();
        kubernetesDiscoveryService.closeClient();

        Mockito.verify(kubernetesClientMock, Mockito.times(1)).close();
    }
}