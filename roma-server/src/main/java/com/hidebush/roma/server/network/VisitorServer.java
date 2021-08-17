package com.hidebush.roma.server.network;

import com.hidebush.roma.util.entity.Protocol;
import com.hidebush.roma.util.network.NettyServer;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.UdpServer;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接服务访问者，将服务访问者的消息发送到 {@link ForwardServer}
 * Created by htf on 2021/8/6.
 */
public class VisitorServer implements NettyServer {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final ForwardServer forwardServer;
    private final ConcurrentMap<ChannelId, Integer> visitorChannelId = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Channel> visitorChannel = new ConcurrentHashMap<>();
    private final AtomicInteger visitorId = new AtomicInteger();

    private final NettyServer nettyServer;

    public VisitorServer(Protocol protocol, int localPort, ForwardServer forwardServer) {
        if (protocol == Protocol.TCP) {
            nettyServer = new TcpServer(localPort) {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new VisitorHandler())
                            .addLast(new ChannelOutboundHandlerAdapter());
                }
            };
        } else {
            nettyServer = new UdpServer(localPort) {
                @Override
                protected void initChannel(NioDatagramChannel ch) {
                    ch.pipeline().addLast(new VisitorUdpHandler())
                            .addLast(new ChannelOutboundHandlerAdapter());
                }
            };
        }
        this.forwardServer = forwardServer;
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("VisitorServer", id);
    }

    public int id() {
        return id;
    }

    public void sendMsgToVisitor(int visitorId, byte[] data) {
        reporter.debug("send to visitor(" + visitorId + ") " + data.length + " bytes");
        Channel channel = visitorChannel.get(visitorId);
        ByteBuf out = channel.alloc().ioBuffer(data.length);
        out.writeBytes(data);
        channel.writeAndFlush(out);
    }

    public void disconnectVisitor(int visitorId) {
        reporter.debug("disconnect visitor(" + visitorId + ")");
        visitorChannel.get(visitorId).close();
    }

    @Override
    public int getLocalPort() {
        return nettyServer.getLocalPort();
    }

    @Override
    public Channel getChannel() {
        return nettyServer.getChannel();
    }

    @Override
    public void startup() {
        nettyServer.startup();
    }

    private class VisitorHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            int visitorId = VisitorServer.this.visitorId.incrementAndGet();
            reporter.debug("visitor(" + visitorId + ") connect");
            visitorChannelId.put(ctx.channel().id(), visitorId);
            visitorChannel.put(visitorId, ctx.channel());
            forwardServer.sendConnectMsgToForwardClient(visitorId);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            if (in.isReadable()) {
                Integer visitorId = visitorChannelId.get(ctx.channel().id());
                byte[] bytes = new byte[in.readableBytes()];
                in.readBytes(bytes);
                reporter.debug("receive from visitor(" + visitorId + ") " + bytes.length + " bytes");
                forwardServer.sendMsgToForwardClient(visitorId, bytes);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Integer visitorId = visitorChannelId.remove(ctx.channel().id());
            reporter.debug("visitor(" + visitorId + ") disconnect");
            forwardServer.sendDisconnectMsgToForwardClient(visitorId);
            Channel channel = visitorChannel.remove(visitorId);
            channel.close();
        }
    }

    private class VisitorUdpHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            DatagramPacket in = (DatagramPacket) msg;
            Integer visitorId = visitorChannelId.computeIfAbsent(ctx.channel().id(), k -> {
                int id = VisitorServer.this.visitorId.incrementAndGet();
                visitorChannel.put(id, ctx.channel());
                forwardServer.sendConnectMsgToForwardClient(id);
                return id;
            });
            ByteBuf content = in.content();
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            reporter.debug("receive from visitor(" + visitorId + ") " + bytes.length + " bytes");
            forwardServer.sendMsgToForwardClient(visitorId, bytes);
        }
    }

}
