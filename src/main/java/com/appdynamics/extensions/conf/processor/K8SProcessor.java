package com.appdynamics.extensions.conf.processor;

import com.appdynamics.extensions.discovery.k8s.KubernetesDiscoveryService;
import com.appdynamics.extensions.discovery.k8s.PodAddress;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class K8SProcessor {

    private static Map<String, ?> baseServerConfig;

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(K8SProcessor.class);

    private static final String HOST_PATTERN = "${host}";
    private static final String PORT_PATTERN = "${port}";

    public static Map<String, ?> process(Map configYml) {
        if (configYml == null) {
            logger.error("Empty configuration passed, ignoring");
            return null;
        }

        Map<String, ?> kubernetesConfig = (Map<String, ?>) configYml.get("kubernetes");

        String kubernetesMode = (String) kubernetesConfig.get("useKubernetes");

        if (kubernetesMode != null && Boolean.valueOf(kubernetesMode)) {

            logger.info("KubernetesMode configured, trying to discover pods which are running nginx");

            if (baseServerConfig == null) {
                List<Map<String, ?>> serversList = (List<Map<String, ?>>) configYml.get("servers");
                baseServerConfig = serversList.get(0);
            }

            String kubernetesNamespace = (String) kubernetesConfig.get("namespace");
            String containerImageNameToMatch = (String) kubernetesConfig.get("containerImageNameToMatch");
            List<Map<String, String>> podLabels = (List<Map<String, String>>) kubernetesConfig.get("podLabels");

            Map<String, String> allPodLabels = Maps.newHashMap();

            if (podLabels != null && podLabels.size() > 0) {

                for (Map<String, String> podLabel : podLabels) {
                    allPodLabels.put(podLabel.get("name"), podLabel.get("value"));
                }
            }

            KubernetesDiscoveryService kubernetesDiscoveryService = new KubernetesDiscoveryService.KubernetesDiscoveryBuilder().withNamespace(kubernetesNamespace).withContainerImage(containerImageNameToMatch)
                    .withLabels(allPodLabels).build();

            List<PodAddress> discoveredPods = null;
            try {
                discoveredPods = kubernetesDiscoveryService.discover();
            } catch (Exception e) {
                logger.error("Error when trying to discover Kubernetes pods", e);
            } finally {
                kubernetesDiscoveryService.closeClient();
            }

            logger.info("Endpoints found {}", discoveredPods);


            List<Map<String, ?>> serversList = (List<Map<String, ?>>) configYml.get("servers");

            serversList.clear();

            Set<? extends Map.Entry<String, ?>> entries = baseServerConfig.entrySet();

            for (PodAddress podAddress : discoveredPods) {
                Map<String, String> updatedServerConfig = Maps.newHashMap();

                for (Map.Entry<String, ?> serverEntry : entries) {
                    String key = serverEntry.getKey();
                    String value = (String) serverEntry.getValue();

                    if (!Strings.isNullOrEmpty(value)) {
                        if (value.contains(HOST_PATTERN)) {
                            value = value.replace(HOST_PATTERN, podAddress.getHost());
                        }

                        if (value.contains(PORT_PATTERN)) {
                            value = value.replace(PORT_PATTERN, String.valueOf(podAddress.getPort()));
                        }
                    }
                    updatedServerConfig.put(key, value);
                }
                serversList.add(updatedServerConfig);
            }

            logger.info("Final servers " + serversList);
        }

        return configYml;
    }
}
