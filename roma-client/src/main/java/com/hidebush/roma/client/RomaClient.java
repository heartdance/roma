package com.hidebush.roma.client;

import com.hidebush.roma.client.config.ClientOption;
import com.hidebush.roma.client.entity.Proxy;
import com.hidebush.roma.client.network.ManagementClient;
import com.hidebush.roma.util.config.CommandLine;
import com.hidebush.roma.util.config.RomaConfig;
import com.hidebush.roma.util.config.StringList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by htf on 2021/8/6.
 */
public class RomaClient {

    private final RomaConfig config;
    private ManagementClient managementClient;

    public RomaClient(RomaConfig config) {
        this.config = config;
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(ClientOption.SERVER_HOST, ClientOption.SERVER_PORT, ClientOption.PROXY);
        RomaClient romaClient = new RomaClient(commandLine.parse(args));
        romaClient.startup();
        List<Proxy> proxies = proxies(romaClient.config.get(ClientOption.PROXY));
        for (Proxy proxy : proxies) {
            romaClient.createProxy(proxy);
        }
    }

    private static List<Proxy> proxies(StringList proxiesConfig) {
        List<Proxy> proxies = new ArrayList<>(proxiesConfig.size());
        for (String proxyConfig : proxiesConfig) {
            try {
                String[] split = proxyConfig.split("-");
                String[] serviceConfig = split[1].split(":");
                proxies.add(new Proxy(Integer.parseInt(split[0]), serviceConfig[0], Integer.parseInt(serviceConfig[1])));
            } catch (Exception e) {
                throw new IllegalArgumentException("proxy '" + proxyConfig + "'");
            }
        }
        return proxies;
    }

    public void startup() {
        managementClient = new ManagementClient(config.get(ClientOption.SERVER_HOST), config.get(ClientOption.SERVER_PORT));
        managementClient.startup();
    }

    public void createProxy(Proxy proxy) {
        managementClient.createProxy(proxy.getPort(), proxy.getServiceHost(), proxy.getServicePort());
    }
}
