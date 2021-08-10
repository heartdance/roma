package com.hidebush.roma.server;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 连接客户端，将客户端消息发送到 {@link VisitorServer}
 * Created by htf on 2021/8/6.
 */
public class ForwardServer extends TcpServer {

    private VisitorServer visitorServer;
    private Channel forwardClientChannel;

    public ForwardServer(int localPort) {
        super(localPort);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 5, 2))
                .addLast(new TlvEncoder(4, 1, 2))
                .addLast(new TlvDecoder(4, 1, 2))
                .addLast(new TlvHandler());
    }

    public void setVisitorServer(VisitorServer visitorServer) {
        this.visitorServer = visitorServer;
    }

    public void sendConnectMsgToForwardClient(int visitorId) {
        System.out.println("send to forwardClient " + visitorId + " connect");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_CONNECT, Bytes.toBytes(visitorId, 4));
        forwardClientChannel.writeAndFlush(tlv);
    }

    public void sendMsgToForwardClient(int visitorId, byte[] data) {
        System.out.println("send to forwardClient " + visitorId + " " + data.length + " bytes");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_SEND_MSG, Bytes.merge(Bytes.toBytes(visitorId, 4), data));
        forwardClientChannel.writeAndFlush(tlv);
    }

    public void sendDisconnectMsgToForwardClient(int visitorId) {
        System.out.println("send to forwardClient " + visitorId + " disconnect");
        Tlv tlv = new Tlv(TypeConstant.ON_VISITOR_DISCONNECT, Bytes.toBytes(visitorId, 4));
        forwardClientChannel.writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("forwardClient connect");
            forwardClientChannel = ctx.channel();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PING) {
                System.out.println("receive from forwardClient ping");
                System.out.println("send to forwardClient pong");
                ctx.writeAndFlush(new Tlv(TypeConstant.PONG));
            } else if (tlv.getType() == TypeConstant.ON_SERVICE_SEND_MSG) {
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value, 0, 4);
                byte[] data = new byte[value.length - 4];
                System.arraycopy(value, 4, data, 0, data.length);
                System.out.println("receive from forwardClient " + visitorId + " " + data.length + " bytes");
                visitorServer.sendMsgToVisitor(visitorId, data);
            } else if (tlv.getType() == TypeConstant.ON_SERVICE_DISCONNECT) {
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value);
                System.out.println("receive from forwardClient " + visitorId + " disconnect");
                visitorServer.disconnectVisitor(visitorId);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            System.out.println("forwardClient disconnect");
            System.out.println("close visitorServer");
            visitorServer.getChannel().close();
            System.out.println("close forwardServer");
            getChannel().close();
        }
    }

}
