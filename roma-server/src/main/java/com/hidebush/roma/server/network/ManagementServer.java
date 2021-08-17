package com.hidebush.roma.server.network;

import com.hidebush.roma.server.entity.ClientInfo;
import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Protocol;
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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理所有代理
 * Created by htf on 2021/8/5.
 */
public class ManagementServer extends TcpServer {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final ConcurrentMap<ChannelId, ClientInfo> clientForwardServers = new ConcurrentHashMap<>();

    public ManagementServer(int localPort) {
        super(localPort);
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ManagementServer", id);
    }

    public int id() {
        return id;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 1, 2))
                .addLast(new IdleStateHandler(300, 0, 0))
                .addLast(new TlvEncoder(1, 2))
                .addLast(new TlvDecoder(1, 2))
                .addLast(new TlvHandler());
    }

    private ForwardServer createForwardServer(ChannelId clientId, Protocol protocol, int visitorServerPort) {
        ForwardServer forwardServer = new ForwardServer();
        forwardServer.startup();
        reporter.info("forwardServer(" + forwardServer.id() + ") bind port " + forwardServer.getLocalPort());
        forwardServer.createVisitorServer(protocol, visitorServerPort);
        clientForwardServers.get(clientId).getForwardServers().add(forwardServer);
        return forwardServer;
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            reporter.info("managementClient connect: " + ctx.channel().remoteAddress());
            clientForwardServers.put(ctx.channel().id(), new ClientInfo(ctx.channel().remoteAddress()));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PING) {
                reporter.debug("receive from managementClient: ping");
                reporter.debug("send to managementClient: pong");
                ctx.writeAndFlush(new Tlv(TypeConstant.PONG));
            } else if (tlv.getType() == TypeConstant.CREATE_TCP_PROXY || tlv.getType() == TypeConstant.CREATE_UDP_PROXY) {
                int port = Bytes.toInt(tlv.getValue(), 4, 2);
                reporter.info("create proxy on port " + port);
                ForwardServer forwardServer = createForwardServer(ctx.channel().id(),
                        tlv.getType() == TypeConstant.CREATE_TCP_PROXY ? Protocol.TCP : Protocol.UDP, port);
                reporter.debug("send to managementClient: proxy on port " + port + " created");
                byte[] bytes = new byte[6];
                System.arraycopy(tlv.getValue(), 0, bytes, 0, 4);
                System.arraycopy(Bytes.toBytes(forwardServer.getLocalPort(), 2), 0,
                        bytes, 4, 2);
                ctx.writeAndFlush(new Tlv(TypeConstant.SUCCESS, bytes));
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.READER_IDLE) {
                    reporter.error("disconnect managementClient " + ctx.channel().remoteAddress() +
                            " because of reader timeout");
                    ctx.channel().close();
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            reporter.info("managementClient disconnect");
            ClientInfo clientInfo = clientForwardServers.remove(ctx.channel().id());
            if (clientInfo != null) {
                reporter.debug("disconnect forwardServers of client " + clientInfo.getSocketAddress());
                for (ForwardServer forwardServer : clientInfo.getForwardServers()) {
                    forwardServer.getChannel().close();
                }
            }
        }
    }

}
