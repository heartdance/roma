package com.hidebush.roma.server;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理所有代理
 * Created by htf on 2021/8/5.
 */
public class ManagementServer extends TcpServer {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final PortManager portManager = new PortManager();
    private final ConcurrentMap<ChannelId, List<ForwardServer>> clientForwardServers = new ConcurrentHashMap<>();

    public ManagementServer(int localPort) {
        super(localPort);
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ManagementServer(" + id + ")");
    }

    public int id() {
        return id;
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
        forwardServer.startup();
        reporter.info("forwardServer(" + forwardServer.id() + ") bind port " + forwardServer.getLocalPort());
        forwardServer.createVisitorServer(visitorServerPort);
        clientForwardServers.computeIfAbsent(clientId, k -> new CopyOnWriteArrayList<>()).add(forwardServer);
        return forwardServer;
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            reporter.info("managementClient connect");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PING) {
                reporter.debug("receive from managementClient: ping");
                reporter.debug("send to managementClient: pong");
                ctx.writeAndFlush(new Tlv(TypeConstant.PONG));
            } else if (tlv.getType() == TypeConstant.CREATE_PROXY) {
                int id = tlv.getId();
                byte[] value = tlv.getValue();
                int port = Bytes.toInt(value, 0, 2);
                reporter.info("create proxy on port " + port);
                ForwardServer forwardServer = createForwardServer(ctx.channel().id(), port);
                reporter.debug("send to managementClient: proxy on port " + port + " created");
                ctx.writeAndFlush(new Tlv(id, TypeConstant.SUCCESS, Bytes.toBytes(forwardServer.getLocalPort(), 2)));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            reporter.info("managementClient disconnect");
            reporter.debug("close forwardServers");
            List<ForwardServer> forwardServers = clientForwardServers.remove(ctx.channel().id());
            for (ForwardServer forwardServer : forwardServers) {
                forwardServer.getChannel().close();
            }
        }
    }

}
