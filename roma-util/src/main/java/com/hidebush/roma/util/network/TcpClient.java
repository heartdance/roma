package com.hidebush.roma.util.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
        EventLoopGroup group = new NioEventLoopGroup(1);
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        TcpClient.this.initChannel(ch);
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
