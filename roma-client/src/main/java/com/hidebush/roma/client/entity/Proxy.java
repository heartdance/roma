package com.hidebush.roma.client.entity;

/**
 * Created by htf on 2021/8/12.
 */
public class Proxy {

    private final Integer port;

    private final String serviceHost;

    private final Integer servicePort;

    public Proxy(Integer port, String serviceHost, Integer servicePort) {
        if (port == null || serviceHost == null || servicePort == null) {
            throw new NullPointerException();
        }
        if (serviceHost.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.port = port;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
    }

    public Integer getPort() {
        return port;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public Integer getServicePort() {
        return servicePort;
    }
}
