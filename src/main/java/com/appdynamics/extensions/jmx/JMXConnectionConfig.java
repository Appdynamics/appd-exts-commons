package com.appdynamics.extensions.jmx;


import com.google.common.base.Strings;

public class JMXConnectionConfig {

    private String serviceUrl;
    private String host;
    private int port;
    private String username;
    private String password;

    public JMXConnectionConfig(String serviceUrl,String username,String password){
        this.serviceUrl = serviceUrl;
        this.username = username;
        this.password = password;
    }

    public JMXConnectionConfig(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getJMXServiceURL() {
        if(Strings.isNullOrEmpty(serviceUrl)) {
            return "service:jmx:rmi:///jndi/rmi://" + getHost() + ":" + getPort() + "/jmxrmi";
        }
        else{
            return serviceUrl;
        }
    }
}
