package com.hidebush.roma.util.network;

import com.hidebush.roma.util.exception.ErrorCode;
import com.hidebush.roma.util.exception.RomaException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by htf on 2021/8/5.
 */
public abstract class TcpServer implements NettyServer {

    private int localPort;
    private Channel channel;

    public TcpServer(int localPort) {
        this.localPort = localPort;
    }

    @Override
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
                InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
                localPort = socketAddress.getPort();
            } else {
                if (localPort == 0) {
                    throw new RomaException(ErrorCode.NO_FREE_PORT);
                }
                throw new RomaException(ErrorCode.PORT_OCCUPIED);
            }
        } catch (InterruptedException ignored) {
        }
    }

    protected abstract void initChannel(SocketChannel ch);

    @Override
    public int getLocalPort() {
        return localPort;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
