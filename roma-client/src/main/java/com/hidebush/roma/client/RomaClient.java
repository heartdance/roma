package com.hidebush.roma.client;

/**
 * Created by htf on 2021/8/6.
 */
public class RomaClient {

    public static void main(String[] args) {
        RomaClient romaClient = new RomaClient();
        romaClient.startup();

    }

    public void startup() {
        ManagementClient managementClient = new ManagementClient("localhost", 9998);
        managementClient.startup();
        managementClient.createProxy(80, "localhost", 8080);
    }
}
