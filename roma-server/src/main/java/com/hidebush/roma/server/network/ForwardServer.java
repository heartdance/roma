package com.hidebush.roma.server.network;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
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
 * 连接客户端，将客户端消息发送到 {@link VisitorServer}
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

    public void createVisitorServer(int port) {
        this.visitorServer = new VisitorServer(port, this);
        reporter.info("visitorServer(" + visitorServer.id() + ") bind port " + port);
        this.visitorServer.startup();
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 1, 2))
                .addLast(new IdleStateHandler(300, 0, 0))
                .addLast(new TlvEncoder(1, 2))
                .addLast(new TlvDecoder(1, 2))
                .addLast(new TlvHandler());
    }

    public void sendConnectMsgToForwardClient(int visitorId) {
        reporter.debug("send to forwardClient: visitor(" + visitorId + ") connect");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_CONNECT, Bytes.toBytes(visitorId, 4));
        forwardClientChannel.writeAndFlush(tlv);
    }

    public void sendMsgToForwardClient(int visitorId, byte[] data) {
        reporter.debug("send to forwardClient " + data.length + " bytes from visitor(" + visitorId + ")");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_SEND_MSG, Bytes.merge(Bytes.toBytes(visitorId, 4), data));
        forwardClientChannel.writeAndFlush(tlv);
    }

    public void sendDisconnectMsgToForwardClient(int visitorId) {
        reporter.debug("send to forwardClient: visitor(" + visitorId + ") disconnect");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_DISCONNECT, Bytes.toBytes(visitorId, 4));
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
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value, 0, 4);
                byte[] data = new byte[value.length - 4];
                System.arraycopy(value, 4, data, 0, data.length);
                reporter.debug("receive from forwardClient: service " + " send " +
                        data.length + " bytes to visitor(" + visitorId + ")");
                visitorServer.sendMsgToVisitor(visitorId, data);
            } else if (tlv.getType() == TypeConstant.ON_SERVICE_DISCONNECT) {
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value);
                reporter.debug("receive from forwardClient: service " + " disconnect to visitor(" + visitorId + ")");
                visitorServer.disconnectVisitor(visitorId);
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.READER_IDLE) {
                    reporter.error("disconnect forwardClient " + ctx.channel().remoteAddress() +
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
