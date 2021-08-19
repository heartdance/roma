package com.hidebush.roma.client.network;

import com.hidebush.roma.util.entity.Protocol;
import com.hidebush.roma.util.network.NettyClient;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.UdpClient;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务客户端，用于连接服务
 * 1.将服务发来的消息发送到 {@link ForwardClient}
 * 2.提供发送消息到服务的接口
 * Created by htf on 2021/8/6.
 */
public class ServiceClient implements NettyClient {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final int visitorId;
    private final ForwardClient forwardClient;

    private final NettyClient nettyClient;

    public ServiceClient(int visitorId, Protocol protocol, String host, int port, ForwardClient forwardClient) {
        this.visitorId = visitorId;
        if (protocol == Protocol.TCP) {
            nettyClient = new TcpClient(host, port) {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new ServiceHandler())
                            .addLast(new ChannelOutboundHandlerAdapter());
                }
            };
        } else {
            nettyClient = new UdpClient(host, port) {
                @Override
                protected void initChannel(NioDatagramChannel ch) {
                    ch.pipeline().addLast(new ServiceUdpHandler())
                            .addLast(new ChannelOutboundHandlerAdapter());
                }
            };
        }
        this.forwardClient = forwardClient;
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ServiceClient", id);
    }

    public int id() {
        return id;
    }

    public void sendMsgToService(ByteBuf data) {
        reporter.debug("send to service " + data.readableBytes() + " bytes");
        getChannel().writeAndFlush(data);
    }

    @Override
    public String getHost() {
        return nettyClient.getHost();
    }

    @Override
    public int getPort() {
        return nettyClient.getPort();
    }

    @Override
    public Channel getChannel() {
        return nettyClient.getChannel();
    }

    @Override
    public void startup() {
        nettyClient.startup();
    }

    private class ServiceHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                ByteBuf in = (ByteBuf) msg;
                if (in.isReadable()) {
                    reporter.debug("receive from service " + in.readableBytes() + " bytes");
                    forwardClient.sendMsgToForwardServer(visitorId, in.retain());
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            reporter.debug("disconnect");
            forwardClient.sendDisconnectMsgToForwardServer(visitorId);
        }
    }

    private class ServiceUdpHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                DatagramPacket in = (DatagramPacket) msg;
                ByteBuf content = in.content();
                reporter.debug("receive from service " + content.readableBytes() + " bytes");
                forwardClient.sendMsgToForwardServer(visitorId, content.retain());
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            reporter.debug("disconnect");
            forwardClient.sendDisconnectMsgToForwardServer(visitorId);
        }
    }
}
