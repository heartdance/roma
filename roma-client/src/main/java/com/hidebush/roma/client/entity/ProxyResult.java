package com.hidebush.roma.client.entity;

/**
 * Created by htf on 2021/8/19.
 */
public class ProxyResult {

    private final boolean success;

    private final int forwardServerPort;

    private final int errorCode;

    private ProxyResult(boolean success, int forwardServerPort, int errorCode) {
        this.success = success;
        this.forwardServerPort = forwardServerPort;
        this.errorCode = errorCode;
    }

    public static ProxyResult success(int forwardServerPort) {
        return new ProxyResult(true, forwardServerPort, 0);
    }

    public static ProxyResult failure(int errorCode) {
        return new ProxyResult(false, 0, errorCode);
    }

    public boolean success() {
        return success;
    }

    public int forwardServerPort() {
        return forwardServerPort;
    }

    public int errorCode() {
        return errorCode;
    }
}
