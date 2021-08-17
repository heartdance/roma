package com.hidebush.roma.util.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Created by htf on 2021/8/16.
 */
public abstract class UdpServer implements NettyServer {

    private int localPort;
    private Channel channel;

    public UdpServer(int localPort) {
        this.localPort = localPort;
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
                        UdpServer.this.initChannel(ch);
                    }
                });
        try {
            ChannelFuture future = b.bind(localPort).sync();
            if (future.isSuccess()) {
                channel = future.channel();
                InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
                localPort = socketAddress.getPort();
            }
        } catch (InterruptedException ignored) {
        }
    }

    protected abstract void initChannel(NioDatagramChannel ch);

    @Override
    public int getLocalPort() {
        return localPort;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
