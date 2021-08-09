package com.hidebush.roma.server;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
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
        Tlv tlv = new Tlv(visitorId, TypeConstant.ON_VISITOR_CONNECT, new byte[0]);
        getChannel().writeAndFlush(tlv);
    }

    public void sendMsgToForwardClient(int visitorId, byte[] data) {
        Tlv tlv = new Tlv(visitorId, TypeConstant.ON_VISITOR_SEND_MSG, data);
        getChannel().writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.ON_SERVICE_SEND_MSG) {
                byte[] value = tlv.getValue();
                int visitorId = Bytes.toInt(value, 0, 4);
                byte[] data = new byte[value.length - 4];
                System.arraycopy(value, 4, data, 0, data.length);
                visitorServer.sendMsgToVisitor(visitorId, data);
            }
        }
    }

}
