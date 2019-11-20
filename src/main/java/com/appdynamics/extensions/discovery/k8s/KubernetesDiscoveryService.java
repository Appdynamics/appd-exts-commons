package com.appdynamics.extensions.discovery.k8s;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes service which discovers the pods and collects its ip and port. The ip and ports are then passed to extension to collect metrics from the
 * service exposed on the specified port.
 *
 * @author Satish Muddam
 */
public class KubernetesDiscoveryService {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(KubernetesDiscoveryService.class);

    // Default namespace is set to "default"
    private String namespace = "default";
    private Map<String, String> labels;
    private String imageName;
    private String containerPortName;
    private KubernetesClient kubernetesClient;
    private final String PHASE_RUNNING = "Running";

    private KubernetesDiscoveryService(KubernetesClient kubernetesClient, String namespace, Map<String, String> labels, String imageName, String containerPortName) {

        if (!Strings.isNullOrEmpty(namespace)) {
            this.namespace = namespace;
        }

        this.kubernetesClient = kubernetesClient;
        this.labels = labels;
        this.imageName = imageName;
        this.containerPortName = containerPortName;
    }

    /**
     * Builder to create KubernetesDiscoveryService
     */
    public static class KubernetesDiscoveryBuilder {

        private String namespace;

        private Map<String, String> labels;

        private String imageName;

        private String containerPortName;

        public KubernetesDiscoveryBuilder withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }


        public KubernetesDiscoveryBuilder withLabel(String name, String value) {
            if (this.labels == null) {
                this.labels = new HashMap<>();
            }
            this.labels.put(name, value);
            return this;
        }

        public KubernetesDiscoveryBuilder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public KubernetesDiscoveryBuilder withContainerImage(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public KubernetesDiscoveryBuilder withContainerPortName(String containerPortName) {
            this.containerPortName = containerPortName;
            return this;
        }

        public KubernetesDiscoveryService build() {
            KubernetesClient kubernetesClientFromBuilder = new KubernetesConnectionBuilder().withNamespace(namespace).build();
            return new KubernetesDiscoveryService(kubernetesClientFromBuilder, namespace, labels, imageName, containerPortName);
        }
    }

    /**
     * Discovers the pods which match the given labels and fetches private ip and port of these pods.
     *
     * @return Pod Endpoint.Address with it's private ip and port
     */
    public List<PodAddress> discover() {

        logger.info("Started Kubernetes discovery for namespace {}", namespace);

        List<Pod> matchedPods = kubernetesClient.pods().withLabels(labels).list().getItems();

        if (matchedPods == null) {
            logger.info("No pods matched the provided labels, stopping the discovery process.");
            return null;
        }

        logger.info("Matched {} pods for the provided label(s)", matchedPods.size());

        List<PodAddress> privateAddressOfPods = extractPrivateAddressOfPods(matchedPods);

        return privateAddressOfPods;
    }

    /**
     * Closes the {@link KubernetesClient}
     */
    public void closeClient() {
        if (kubernetesClient != null) {
            try {
                logger.info("Closing Kubernetes client");
                kubernetesClient.close();
            } catch (Exception e) {
                logger.error("Error closing Kubernetes client", e);
            }
        }
    }

    /**
     * Extracts the pods private ip and port
     *
     * @param pods
     * @return
     */
    private List<PodAddress> extractPrivateAddressOfPods(List<Pod> pods) {

        List<PodAddress> endpoints = new ArrayList<PodAddress>();

        for (Pod pod : pods) {
            String phase = pod.getStatus().getPhase();
            String podIP = pod.getStatus().getPodIP();
            if (PHASE_RUNNING.equalsIgnoreCase(phase) && podIP != null) {
                Integer port = extractContainerPort(pod);
                if (port == null) {
                    logger.error("Could not get port for pod with ip [{}].", podIP);
                } else {
                    endpoints.add(new PodAddress(podIP, port));
                }
            } else {
                logger.info("Ignoring pod [{}] as either the ip is null or the status is not Running.", pod.getMetadata().getName());
            }
        }
        return endpoints;
    }

    /**
     * Extracts the pods port, which is the the container port in this pod. Containers are filtered using the image name passed
     * and extracts the port of the matched container.
     *
     * @param pod
     * @return pods private port
     */
    private Integer extractContainerPort(Pod pod) {
        List<Container> containers = pod.getSpec().getContainers();
        for (Container container : containers) {
            String image = container.getImage();

            boolean matches = image.matches(".*" + this.imageName + ".*");
            if (!matches) {
                continue;
            }
            return getPort(container);
        }
        return null;
    }

    /**
     * Extracts the port of the Pod's container
     *
     * @param container
     * @return
     */
    private Integer getPort(Container container) {
        List<ContainerPort> containerPorts = container.getPorts();
        if (containerPorts.size() == 1) {
            Integer containerPort = containerPorts.get(0).getContainerPort();
            return containerPort;
        } else {
            if (Strings.isNullOrEmpty(containerPortName)) {
                throw new KubernetesClientException("Not able to determine the port of the container, found multiple and containerPortName not configured.");
            } else {
                for (ContainerPort containerPort : containerPorts) {
                    if (containerPortName.equalsIgnoreCase(containerPort.getName())) {
                        return containerPort.getContainerPort();
                    }
                }
                throw new KubernetesClientException("Not able to determine the port of the container, found multiple and containerPortName did not match");
            }
        }
    }
}