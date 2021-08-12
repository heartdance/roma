package com.hidebush.roma.client.config;

import com.hidebush.roma.util.config.Option;
import com.hidebush.roma.util.config.StringList;

/**
 * Created by htf on 2021/8/12.
 */
public class ClientOption {

    public static final Option<String> SERVER_HOST = new Option<>(
            "server_host", null, String.class, "h", "host", "server_host");

    public static final Option<Integer> SERVER_PORT = new Option<>(
            "server_port", 9989, Integer.class, "p","port", "server_port");

    public static final Option<StringList> PROXY = new Option<>(
            "proxy", new StringList(), StringList.class,"proxy");
}
