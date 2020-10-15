package cn.flypigeon.roma.server.manager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by htf on 2020/9/27.
 */
public class SimplePortManager implements PortManager {

    private AtomicInteger proxyPort = new AtomicInteger(56700);

    @Override
    public int getFreePort() {
        return proxyPort.getAndIncrement();
    }

    @Override
    public boolean isFree(int port) {
        return proxyPort.get() > port;
    }
}
