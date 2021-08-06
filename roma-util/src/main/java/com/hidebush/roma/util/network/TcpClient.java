package com.hidebush.roma.util.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by htf on 2021/8/6.
 */
public abstract class TcpClient {

    private final String host;
    private final int port;
    private Channel channel;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startup() {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        TcpClient.this.initChannel(ch);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128);
        try {
            ChannelFuture future = b.connect(host, port).sync();
            if (future.isSuccess()) {
                channel = future.channel();
            }
        } catch (InterruptedException ignored) {
        }
    }

    protected abstract void initChannel(SocketChannel ch);

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Channel getChannel() {
        return channel;
    }
}
