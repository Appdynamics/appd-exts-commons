package com.appdynamics.extensions.discovery.k8s;

/**
 * PodAddress to hold pod's private ip and port.
 *
 * @author Satish Muddam
 */
public class PodAddress {

    private String host;
    private Integer port;

    public PodAddress(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    // TODO PN are we using this anywhere?
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PodAddress address = (PodAddress) o;

        if (host != null ? !host.equals(address.host) : address.host != null) {
            return false;
        }
        return port != null ? port.equals(address.port) : address.port == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", host, port);
    }

}




