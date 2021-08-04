package com.hidebush.roma.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by htf on 2020/9/27.
 */
public class ForwardConfig {

    private Host serverHost;
    private List<Host> hosts = new ArrayList<>();

    public ForwardConfig(String[] args) {
        if (args == null || args.length == 0) {
            serverHost = new Host("localhost", 9998);
            hosts.add(new Host("localhost", 80));
        } else {
            serverHost = new Host(args[0]);
            if (args.length == 1) {
                hosts.add(new Host("localhost", 80));
            } else {
                for (int i = 1; i < args.length; i++) {
                    hosts.add(new Host(args[i]));
                }
            }
        }
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public Host getServerHost() {
        return serverHost;
    }

    public static class Host {
        private String ip;
        private int port;

        public Host(String conf) {
            if (conf.contains(":")) {
                String[] split = conf.split(":");
                this.ip = split[0];
                this.port = Integer.parseInt(split[1]);
            } else {
                this.ip = "localhost";
                this.port = Integer.parseInt(conf);
            }
        }

        public Host(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }
    }
}
