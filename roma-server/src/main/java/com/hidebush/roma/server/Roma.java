package com.hidebush.roma.server;

/**
 * Created by htf on 2021/8/6.
 */
public class Roma {

    public static void main(String[] args) {
        Roma roma = new Roma();
        roma.startup();
    }

    private ManagementServer managementServer;

    public void startup() {
        managementServer = new ManagementServer(9998);
        managementServer.startup();
    }
}
