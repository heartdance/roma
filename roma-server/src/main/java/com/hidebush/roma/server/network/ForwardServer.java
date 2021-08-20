package com.hidebush.roma.server.network;

import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Protocol;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.exception.ExceptionHandler;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 转发服务端，用于接受转发客户端的连接
 * 1.创建和管理 {@link VisitorServer}
 * 2.转发客户端消息到 {@link VisitorServer}
 * 3.提供转发消息到转发客户端的接口
 * Created by htf on 2021/8/6.
 */
public class ForwardServer extends TcpServer {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private VisitorServer visitorServer;
    private Channel forwardClientChannel;

    public ForwardServer() {
        super(0);
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ForwardServer", id);
    }

    public int id() {
        return id;
    }

    public void createVisitorServer(Protocol protocol, int port) {
        this.visitorServer = new VisitorServer(protocol, port, this);
        reporter.info("visitorServer(" + visitorServer.id() + ") bind port " + port);
        this.visitorServer.startup();
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                32 * 1024 * 1024, 1, 4))
                .addLast(new IdleStateHandler(300, 0, 0))
                .addLast(new TlvEncoder(1, 4))
                .addLast(new TlvDecoder(1, 4))
                .addLast(new TlvHandler())
                .addLast(new ExceptionHandler(reporter));
    }

    public void sendConnectMsgToForwardClient(int visitorId) {
        reporter.debug("send to forwardClient: visitor(" + visitorId + ") connect");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_CONNECT, Unpooled.copyInt(visitorId));
        forwardClientChannel.writeAndFlush(tlv);
    }

    public void sendMsgToForwardClient(int visitorId, ByteBuf data) {
        reporter.debug("send to forwardClient " + data.readableBytes() + " bytes from visitor(" + visitorId + ")");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_SEND_MSG, Unpooled.wrappedBuffer(Unpooled.copyInt(visitorId), data));
        forwardClientChannel.writeAndFlush(tlv);
    }

    public void sendDisconnectMsgToForwardClient(int visitorId) {
        reporter.debug("send to forwardClient: visitor(" + visitorId + ") disconnect");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_DISCONNECT, Unpooled.copyInt(visitorId));
        forwardClientChannel.writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            reporter.debug("forwardClient connect");
            forwardClientChannel = ctx.channel();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PING) {
                reporter.debug("receive from forwardClient: ping");
                reporter.debug("send to forwardClient: pong");
                ctx.writeAndFlush(new Tlv(TypeConstant.PONG));
            } else if (tlv.getType() == TypeConstant.ON_SERVICE_SEND_MSG) {
                int visitorId = tlv.getValue().readInt();
                reporter.debug("receive from forwardClient: service send " +
                        tlv.getValue().readableBytes() + " bytes to visitor(" + visitorId + ")");
                visitorServer.sendMsgToVisitor(visitorId, tlv.getValue().retain());
            } else if (tlv.getType() == TypeConstant.ON_SERVICE_DISCONNECT) {
                int visitorId = tlv.getValue().readInt();
                reporter.debug("receive from forwardClient: service " +
                        " disconnect to visitor(" + visitorId + ")");
                visitorServer.disconnectVisitor(visitorId);
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.READER_IDLE) {
                    reporter.warn("disconnect forwardClient " + ctx.channel().remoteAddress() +
                            " because of reader timeout");
                    ctx.channel().close();
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            reporter.debug("forwardClient disconnect");
            reporter.debug("close visitorServer(" + visitorServer.id() + ")");
            visitorServer.getChannel().close();
            reporter.debug("close");
            getChannel().close();
        }
    }

}
