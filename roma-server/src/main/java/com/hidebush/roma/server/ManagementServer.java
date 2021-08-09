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
 * Created by htf on 2021/8/5.
 */
public class ManagementServer extends TcpServer {

    private final PortManager portManager = new PortManager();

    public ManagementServer(int localPort) {
        super(localPort);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 5, 2))
                .addLast(new TlvEncoder(4, 1, 2))
                .addLast(new TlvDecoder(4, 1, 2))
                .addLast(new TlvHandler());
    }

    private void createForwardServer(int visitorServerPort) {
        ForwardServer forwardServer = new ForwardServer(portManager.getFreePort());
        VisitorServer visitorServer = new VisitorServer(visitorServerPort, forwardServer);
        forwardServer.setVisitorServer(visitorServer);
        forwardServer.startup();
        visitorServer.startup();
    }

    private void sendToManagementClient(Tlv tlv) {
        getChannel().writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.CREATE_PROXY) {
                int id = tlv.getId();
                byte[] value = tlv.getValue();
                int port = Bytes.toInt(value, 0, 2);
                createForwardServer(port);
                sendToManagementClient(new Tlv(id, TypeConstant.SUCCESS, new byte[0]));
            }
        }
    }

}
