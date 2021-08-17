package com.hidebush.roma.util.network;

import io.netty.channel.Channel;

/**
 * Created by htf on 2021/8/17.
 */
public interface NettyClient {

    String getHost();

    int getPort();

    Channel getChannel();

    void startup();
}
