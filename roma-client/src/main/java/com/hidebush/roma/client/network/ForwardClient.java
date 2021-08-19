package com.hidebush.roma.client.network;

import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Protocol;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 转发客户端，用于连接转发服务端
 * 1.创建和管理 {@link ServiceClient}
 * 2.转发服务端消息到 {@link ServiceClient}
 * 3.提供转发消息到转发服务端的接口
 * Created by htf on 2021/8/6.
 */
public class ForwardClient extends TcpClient {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final Protocol protocol;
    private final String serviceHost;
    private final int servicePort;

    private final ConcurrentMap<Integer, ServiceClient> serviceClientMap = new ConcurrentHashMap<>();

    public ForwardClient(String host, int port, Protocol protocol, String serviceHost, int servicePort) {
        super(host, port);
        this.protocol = protocol;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ForwardClient", id);
    }

    public int id() {
        return id;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                32 * 1024 * 1024, 1, 4))
                .addLast(new IdleStateHandler(300, 30, 0))
                .addLast(new TlvEncoder(1, 4))
                .addLast(new TlvDecoder(1, 4))
                .addLast(new TlvHandler());
    }

    private void createServiceClient(int visitorId) {
        ServiceClient serviceClient = new ServiceClient(visitorId, protocol, serviceHost, servicePort, this);
        serviceClientMap.put(visitorId, serviceClient);
        serviceClient.startup();
        reporter.debug("serviceClient(" + serviceClient.id() + ") connect to " + serviceHost + ":" + servicePort);
    }

    public void sendMsgToForwardServer(int visitorId, ByteBuf data) {
        ServiceClient serviceClient = serviceClientMap.get(visitorId);
        reporter.debug("send to forwardServer: serviceClient(" + serviceClient.id() +
                ") send " + data.readableBytes() + " bytes to visitor(" + visitorId + ")");
        Tlv tlv = new Tlv(TypeConstant.ON_SERVICE_SEND_MSG, Unpooled.wrappedBuffer(Unpooled.copyInt(visitorId), data));
        getChannel().writeAndFlush(tlv);
    }

    public void sendDisconnectMsgToForwardServer(int visitorId) {
        ServiceClient serviceClient = serviceClientMap.remove(visitorId);
        Integer serviceClientId = serviceClient == null ? null : serviceClient.id();
        reporter.debug("send to forwardServer: serviceClient(" + serviceClientId +
                ") disconnect to visitor(" + visitorId + ")");
        Tlv tlv = new Tlv(TypeConstant.ON_SERVICE_DISCONNECT, Unpooled.copyInt(visitorId));
        getChannel().writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PONG) {
                reporter.debug("receive from forwardServer: pong");
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_CONNECT) {
                int visitorId = tlv.getValue().readInt();
                reporter.debug("receive from forwardServer: visitor(" + visitorId + ") connect");
                createServiceClient(visitorId);
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_SEND_MSG) {
                int visitorId = tlv.getValue().readInt();
                ServiceClient serviceClient = serviceClientMap.get(visitorId);
                if (serviceClient != null) {
                    reporter.debug("receive from forwardServer: visitor(" + visitorId +
                            ") send " + tlv.getValue().readableBytes() + " bytes");
                    serviceClient.sendMsgToService(tlv.getValue().retain());
                }
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_DISCONNECT) {
                int visitorId = tlv.getValue().readInt();
                reporter.debug("receive from forwardServer: visitor(" + visitorId + ") disconnect");
                ServiceClient serviceClient = serviceClientMap.remove(visitorId);
                reporter.debug("disconnect serviceClient(" +
                        (serviceClient == null ? null : serviceClient.id()) + ")");
                if (serviceClient != null) {
                    serviceClient.getChannel().close();
                }
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                reporter.debug("send to forwardServer: ping");
                ctx.channel().writeAndFlush(new Tlv(TypeConstant.PING));
            } else if (state == IdleState.READER_IDLE) {
                reporter.error("disconnect forwardServer because of reader time out");
                ctx.channel().close();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            reporter.debug("forwardServer disconnect");
            super.channelInactive(ctx);
        }
    }

}
