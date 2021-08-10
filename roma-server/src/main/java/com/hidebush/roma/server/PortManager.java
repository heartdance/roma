package com.hidebush.roma.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 提供一个空闲端口
 * Created by htf on 2021/8/9.
 */
public class PortManager {

    private final AtomicInteger port = new AtomicInteger(23000);

    public int getFreePort() {
        return port.incrementAndGet();
    }
}
