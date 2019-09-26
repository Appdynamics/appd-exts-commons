package com.appdynamics.extensions.discovery.k8s;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;

/**
 * Creates a kubernetes client which connects to the kubernetes cluster where this is deployed
 *
 * @author Satish Muddam
 */
public class KubernetesConnectionBuilder {

    public static final Logger logger = ExtensionsLoggerFactory.getLogger(KubernetesConnectionBuilder.class);

    private String namespace;
    private KubernetesClient kubernetesClient;

    public KubernetesConnectionBuilder withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public KubernetesClient build() {
        logger.info("Connecting to Kubernetes in the cluster");

        Config kubeConfig = new ConfigBuilder()
                .withNamespace(namespace)
                .build();
        kubernetesClient = new DefaultKubernetesClient(kubeConfig);
        return kubernetesClient;
    }


}