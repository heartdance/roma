package com.hidebush.roma.client;

/**
 * Created by htf on 2021/8/6.
 */
public class RomaClient {

    public static void main(String[] args) {
        RomaClient romaClient = new RomaClient();

    }

    private ManagementClient managementClient;

    public void startup() {
        managementClient = new ManagementClient("localhost", 9998);
        managementClient.startup();

    }
}
