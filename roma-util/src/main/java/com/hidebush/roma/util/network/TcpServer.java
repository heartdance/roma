package com.hidebush.roma.util.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by htf on 2021/8/5.
 */
public abstract class TcpServer {

    private final int localPort;
    private Channel channel;

    public TcpServer(int localPort) {
        this.localPort = localPort;
    }

    public void startup() {
        ServerBootstrap b = new ServerBootstrap();
        EventLoopGroup group = new NioEventLoopGroup(1);

        b.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        TcpServer.this.initChannel(ch);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
        try {
            ChannelFuture future = b.bind(localPort).sync();
            if (future.isSuccess()) {
                channel = future.channel();
            }
        } catch (InterruptedException ignored) {
        }
    }

    protected abstract void initChannel(SocketChannel ch);

    public int getLocalPort() {
        return localPort;
    }

    public Channel getChannel() {
        return channel;
    }
}
