package com.hidebush.roma.server.entity;

import com.hidebush.roma.server.network.ForwardServer;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by htf on 2021/8/13.
 */
public class ClientInfo {

    private final SocketAddress socketAddress;

    private final List<ForwardServer> forwardServers = new CopyOnWriteArrayList<>();

    public ClientInfo(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public List<ForwardServer> getForwardServers() {
        return forwardServers;
    }
}
