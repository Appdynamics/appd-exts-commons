package com.appdynamics.extensions.conf.modules;

import java.util.Map;

public class KubernetesDiscoveryModule {

    public boolean isK8sDiscoveryEnabled(final Map<String, ?> config){
        //TODO implement this
        return false;
    }



    public boolean updateDiscoveredServers(Map<String, ?> config) {
        //TODO cache the servers section and return true when cache is null or there is a cache miss
        return false;
    }
}
