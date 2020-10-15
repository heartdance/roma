package cn.flypigeon.roma.server.manager;

/**
 * Created by htf on 2020/9/28.
 */
public interface PortManager {

    int getFreePort();

    boolean isFree(int port);
}
