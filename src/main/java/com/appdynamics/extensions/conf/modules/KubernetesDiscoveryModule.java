package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.discovery.k8s.KubernetesDiscoveryService;
import com.appdynamics.extensions.discovery.k8s.PodAddress;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class KubernetesDiscoveryModule {

    private static Map<String, ?> baseServerConfig;

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(KubernetesDiscoveryModule.class);

    private static final String HOST_PATTERN = "${host}";
    private static final String PORT_PATTERN = "${port}";

    private static final Cache<String, String> serversCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();


    public boolean isK8sDiscoveryEnabled(final Map<String, ?> config) {
        Map<String, ?> kubernetesConfig = (Map<String, ?>) config.get("kubernetes");
        String kubernetesMode = (String) kubernetesConfig.get("useKubernetes");
        return Boolean.valueOf(kubernetesMode);
    }


    public boolean updateDiscoveredServers(Map<String, ?> config) {
        logger.info("KubernetesMode configured, trying to discover pods which are running nginx");

        if (baseServerConfig == null) {
            List<Map<String, ?>> serversList = (List<Map<String, ?>>) config.get("servers");
            baseServerConfig = serversList.get(0);
        }

        Map<String, ?> kubernetesConfig = (Map<String, ?>) config.get("kubernetes");

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

        List<Map<String, ?>> serversList = (List<Map<String, ?>>) config.get("servers");

        serversList.clear();

        Set<? extends Map.Entry<String, ?>> entries = baseServerConfig.entrySet();

        int serverCount = 0;
        String serverName = "Server";
        boolean isServersUpdated = false;
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

            String present = serversCache.getIfPresent(podAddress.getHost() + ":" + podAddress.getPort());
            if (present == null) {
                isServersUpdated = true;
                serversCache.put(podAddress.getHost() + ":" + podAddress.getPort(), serverName + serverCount++);
            }

            serversList.add(updatedServerConfig);
        }

        logger.debug("Final servers " + serversList);
        return isServersUpdated;
    }
}