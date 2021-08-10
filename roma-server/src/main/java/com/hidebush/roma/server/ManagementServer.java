package com.hidebush.roma.server;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理所有代理
 * Created by htf on 2021/8/5.
 */
public class ManagementServer extends TcpServer {

    private final PortManager portManager = new PortManager();
    private final ConcurrentMap<ChannelId, TcpServer[]> client = new ConcurrentHashMap<>();

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

    private ForwardServer createForwardServer(ChannelId clientId, int visitorServerPort) {
        ForwardServer forwardServer = new ForwardServer(portManager.getFreePort());
        VisitorServer visitorServer = new VisitorServer(visitorServerPort, forwardServer);
        forwardServer.setVisitorServer(visitorServer);
        forwardServer.startup();
        visitorServer.startup();
        client.put(clientId, new TcpServer[] {forwardServer, visitorServer});
        return forwardServer;
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("managementClient connect");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PING) {
                System.out.println("receive from managementClient ping");
                System.out.println("send to managementClient pong");
                ctx.writeAndFlush(new Tlv(TypeConstant.PONG));
            } else if (tlv.getType() == TypeConstant.CREATE_PROXY) {
                int id = tlv.getId();
                byte[] value = tlv.getValue();
                int port = Bytes.toInt(value, 0, 2);
                System.out.println("start create proxy on port " + port);
                ForwardServer forwardServer = createForwardServer(ctx.channel().id(), port);
                System.out.println("send to managementClient create proxy on port " + port + " success");
                ctx.writeAndFlush(new Tlv(id, TypeConstant.SUCCESS, Bytes.toBytes(forwardServer.getLocalPort(), 2)));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            System.out.println("managementClient disconnect");
            System.out.println("close forwardServer and visitorServer");
            TcpServer[] tcpServers = client.remove(ctx.channel().id());
            for (TcpServer tcpServer : tcpServers) {
                tcpServer.getChannel().close();
            }
        }
    }

}
