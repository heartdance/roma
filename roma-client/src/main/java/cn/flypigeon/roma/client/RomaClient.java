package cn.flypigeon.roma.client;

/**
 * Created by htf on 2020/9/27.
 */
public class RomaClient {
    public static void main(String[] args) {
        args = new String[]{"188.131.238.102:9998", "localhost:8080"};
        ForwardConfig forwardConfig = new ForwardConfig(args);
        ForwardConfig.Host serverHost = forwardConfig.getServerHost();
        for (ForwardConfig.Host host : forwardConfig.getHosts()) {
            LocalTcpForwardServer forwardServer = new LocalTcpForwardServer(
                    serverHost.getIp(), serverHost.getPort(), host.getIp(), host.getPort());
            forwardServer.startup();
        }
    }
}
