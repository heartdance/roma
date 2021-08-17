package com.hidebush.roma.util.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by htf on 2021/8/16.
 */
public abstract class UdpClient implements NettyClient {

    private final String host;
    private final int port;
    private Channel channel;

    public UdpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void startup() {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup(1);
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(NioDatagramChannel ch) {
                        UdpClient.this.initChannel(ch);
                    }
                });
        try {
            ChannelFuture future = b.connect(host, port).sync();
            if (future.isSuccess()) {
                channel = future.channel();
            }
        } catch (InterruptedException ignored) {
        }
    }

    protected abstract void initChannel(NioDatagramChannel ch);

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
