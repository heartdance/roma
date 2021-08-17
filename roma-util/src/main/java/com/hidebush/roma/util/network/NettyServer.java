package com.hidebush.roma.util.network;

import io.netty.channel.Channel;

/**
 * Created by htf on 2021/8/17.
 */
public interface NettyServer {

    int getLocalPort();

    Channel getChannel();

    void startup();
}
