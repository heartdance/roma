package com.hidebush.roma.server;

import com.hidebush.roma.server.config.ServerOption;
import com.hidebush.roma.server.network.ManagementServer;
import com.hidebush.roma.util.config.CommandLine;
import com.hidebush.roma.util.config.RomaConfig;

/**
 * Created by htf on 2021/8/6.
 */
public class Roma {

    private final RomaConfig config;

    public Roma(RomaConfig config) {
        this.config = config;
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(ServerOption.PORT);
        Roma roma = new Roma(commandLine.parse(args));
        roma.startup();
    }

    public void startup() {
        ManagementServer managementServer = new ManagementServer(config.get(ServerOption.PORT));
        managementServer.startup();
    }
}
