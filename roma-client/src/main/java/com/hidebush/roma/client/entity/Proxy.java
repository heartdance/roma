package com.hidebush.roma.client.entity;

import com.hidebush.roma.util.entity.Protocol;

/**
 * Created by htf on 2021/8/12.
 */
public final class Proxy {

    private final Protocol protocol;

    private final Integer port;

    private final String serviceHost;

    private final Integer servicePort;

    public Proxy(Protocol protocol, Integer port, String serviceHost, Integer servicePort) {
        if (protocol == null || port == null || serviceHost == null || servicePort == null) {
            throw new NullPointerException();
        }
        if (serviceHost.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.protocol = protocol;
        this.port = port;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
    }

    public Protocol getProtocol() {
        return protocol;
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

    public String toString() {
        return protocol + " - " + port + " - " + serviceHost + ":" + servicePort;
    }
}
