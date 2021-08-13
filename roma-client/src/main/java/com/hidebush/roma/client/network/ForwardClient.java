package com.hidebush.roma.client.network;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
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
 * 连接服务端，转发服务端消息到 {@link ServiceClient}
 * Created by htf on 2021/8/6.
 */
public class ForwardClient extends TcpClient {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final String serviceHost;
    private final int servicePort;

    private final ConcurrentMap<Integer, ServiceClient> serviceClientMap = new ConcurrentHashMap<>();

    public ForwardClient(String host, int port, String serviceHost, int servicePort) {
        super(host, port);
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
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 1, 2))
                .addLast(new IdleStateHandler(300, 10, 0))
                .addLast(new TlvEncoder(1, 2))
                .addLast(new TlvDecoder(1, 2))
                .addLast(new TlvHandler());
    }

    private void createServiceClient(int visitorId) {
        ServiceClient serviceClient = new ServiceClient(visitorId, serviceHost, servicePort, this);
        serviceClientMap.put(visitorId, serviceClient);
        serviceClient.startup();
        reporter.debug("serviceClient(" + serviceClient.id() + ") connect to " + serviceHost + ":" + servicePort);
    }

    public void sendMsgToForwardServer(int visitorId, byte[] data) {
        ServiceClient serviceClient = serviceClientMap.get(visitorId);
        reporter.debug("send to forwardServer: serviceClient(" + serviceClient.id() +
                ") send " + data.length + " bytes to visitor(" + visitorId + ")");
        Tlv tlv = new Tlv(TypeConstant.ON_SERVICE_SEND_MSG,
                Bytes.merge(Bytes.toBytes(visitorId, 4), data));
        getChannel().writeAndFlush(tlv);
    }

    public void sendDisconnectMsgToForwardServer(int visitorId) {
        ServiceClient serviceClient = serviceClientMap.remove(visitorId);
        reporter.debug("send to forwardServer: serviceClient(" + (serviceClient == null ? null : serviceClient.id()) +
                ") disconnect to visitor(" + visitorId + ")");
        Tlv tlv = new Tlv(TypeConstant.ON_SERVICE_DISCONNECT, Bytes.toBytes(visitorId, 4));
        getChannel().writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PONG) {
                reporter.debug("receive from forwardServer: pong");
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_CONNECT) {
                int visitorId = Bytes.toInt(tlv.getValue());
                reporter.debug("receive from forwardServer: visitor(" + visitorId + ") connect");
                createServiceClient(visitorId);
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_SEND_MSG) {
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value, 0, 4);
                ServiceClient serviceClient = serviceClientMap.get(visitorId);
                if (serviceClient != null) {
                    byte[] bytes = new byte[value.length - 4];
                    System.arraycopy(value, 4, bytes, 0, bytes.length);
                    reporter.debug("receive from forwardServer: visitor(" + visitorId +
                            ") send " + bytes.length + " bytes");
                    serviceClient.sendMsgToService(bytes);
                }
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_DISCONNECT) {
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value);
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
