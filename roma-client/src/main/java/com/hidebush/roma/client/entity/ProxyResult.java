package com.hidebush.roma.client.entity;

/**
 * Created by htf on 2021/8/19.
 */
public class ProxyResult {

    private final int type;

    private final int forwardServerPort;

    public ProxyResult(int type, int forwardServerPort) {
        this.type = type;
        this.forwardServerPort = forwardServerPort;
    }

    public int getType() {
        return type;
    }

    public int getForwardServerPort() {
        return forwardServerPort;
    }
}
