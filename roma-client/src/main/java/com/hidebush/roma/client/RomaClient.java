package com.hidebush.roma.client;

import com.hidebush.roma.client.config.ClientOption;
import com.hidebush.roma.client.entity.Proxy;
import com.hidebush.roma.client.network.ManagementClient;
import com.hidebush.roma.util.config.CommandLine;
import com.hidebush.roma.util.config.RomaConfig;
import com.hidebush.roma.util.config.StringList;
import com.hidebush.roma.util.entity.Protocol;

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
        CommandLine commandLine = new CommandLine(ClientOption.SERVER_HOST, ClientOption.SERVER_PORT,
                ClientOption.TCP_PROXY, ClientOption.UDP_PROXY);
        RomaClient romaClient = new RomaClient(commandLine.parse(args));
        romaClient.startup();

        List<Proxy> proxies = proxies(romaClient.config.get(ClientOption.TCP_PROXY), Protocol.TCP);
        for (Proxy proxy : proxies) {
            romaClient.getManagementClient().createProxy(proxy);
        }

        proxies = proxies(romaClient.config.get(ClientOption.UDP_PROXY), Protocol.UDP);
        for (Proxy proxy : proxies) {
            romaClient.getManagementClient().createProxy(proxy);
        }
    }

    private static List<Proxy> proxies(StringList proxiesConfig, Protocol type) {
        List<Proxy> proxies = new ArrayList<>(proxiesConfig.size());
        for (String proxyConfig : proxiesConfig) {
            try {
                String[] split = proxyConfig.split("/");
                String[] serviceConfig = split[1].split(":");
                proxies.add(new Proxy(type, Integer.parseInt(split[0]),
                        serviceConfig[0], Integer.parseInt(serviceConfig[1])));
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

    public ManagementClient getManagementClient() {
        return managementClient;
    }
}
