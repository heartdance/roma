package com.hidebush.roma.server.config;

import com.hidebush.roma.util.config.Option;

/**
 * Created by htf on 2021/8/12.
 */
public class ServerOption {

    public static final Option<Integer> PORT = new Option<>(
            "port", 9989, Integer.class, "p", "port");
}
