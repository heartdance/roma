package com.hidebush.roma.server;

import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接服务访问者，将服务访问者的消息发送到 {@link ForwardServer}
 * Created by htf on 2021/8/6.
 */
public class VisitorServer extends TcpServer {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final ForwardServer forwardServer;
    private final ConcurrentMap<ChannelId, Integer> visitorChannelId = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Channel> visitorChannel = new ConcurrentHashMap<>();
    private final AtomicInteger visitorId = new AtomicInteger();

    public VisitorServer(int localPort, ForwardServer forwardServer) {
        super(localPort);
        this.forwardServer = forwardServer;
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("VisitorServer(" + id + ")");
    }

    public int id() {
        return id;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new VisitorHandler());
    }

    public void sendMsgToVisitor(int visitorId, byte[] data) {
        reporter.debug("send to visitor(" + visitorId + ") " + data.length + " bytes");
        visitorChannel.get(visitorId).writeAndFlush(new Tlv(TypeConstant.ON_VISITOR_SEND_MSG, data));
    }

    public void disconnectVisitor(int visitorId) {
        reporter.debug("disconnect visitor(" + visitorId + ")");
        visitorChannel.get(visitorId).close();
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

}
