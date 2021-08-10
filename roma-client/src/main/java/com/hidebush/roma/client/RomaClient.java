package com.hidebush.roma.client;

/**
 * Created by htf on 2021/8/6.
 */
public class RomaClient {

    public static void main(String[] args) {
        RomaClient romaClient = new RomaClient();
        romaClient.startup();
        romaClient.createProxy(80, "localhost", 8080);
        romaClient.createProxy(81, "127.0.0.1", 8081);
    }

    private ManagementClient managementClient;

    public void startup() {
        managementClient = new ManagementClient("localhost", 9998);
        managementClient.startup();
    }

    public void createProxy(int port, String serviceHost, int servicePort) {
        managementClient.createProxy(port, serviceHost, servicePort);
    }
}
