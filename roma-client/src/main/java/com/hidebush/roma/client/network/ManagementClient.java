package com.hidebush.roma.client.network;

import com.hidebush.roma.client.entity.Proxy;
import com.hidebush.roma.client.entity.ProxyResult;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.SocketFuture;
import com.hidebush.roma.util.entity.Protocol;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.exception.RomaException;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
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
 * 管理客户端，用于连接管理服务端
 * 创建和管理本地代理
 * Created by htf on 2021/8/6.
 */
public class ManagementClient extends TcpClient {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final AtomicInteger requestId = new AtomicInteger();

    private final ConcurrentMap<Integer, SocketFuture<ProxyResult>> requestFuture = new ConcurrentHashMap<>();

    public ManagementClient(String host, int port) {
        super(host, port);
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ManagementClient", id);
    }

    public int id() {
        return id;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 1, 2))
                .addLast(new IdleStateHandler(300, 30, 0))
                .addLast(new TlvEncoder(1, 2))
                .addLast(new TlvDecoder(1, 2))
                .addLast(new TlvHandler());
    }

    private ProxyResult sendCreateProxyMsgAndGetResponse(Protocol protocol, int proxyPort) {
        int id = requestId.incrementAndGet();
        SocketFuture<ProxyResult> future = new SocketFuture<>();
        requestFuture.put(id, future);
        ByteBuf out = getChannel().alloc().ioBuffer(6);
        out.writeInt(id).writeShort(proxyPort);
        int type = protocol == Protocol.TCP ? TypeConstant.CREATE_TCP_PROXY : TypeConstant.CREATE_UDP_PROXY;
        getChannel().writeAndFlush(new Tlv(type, out));
        try {
            return future.get();
        } catch (InterruptedException e) {
            return null;
        }
    }

    private void createForwardClient(String host, int port, Protocol protocol, String serviceHost, int servicePort) {
        ForwardClient forwardClient = new ForwardClient(host, port, protocol, serviceHost, servicePort);
        forwardClient.startup();
        reporter.info("forwardClient(" + forwardClient.id() + ") connect to " + host + ":" + port +
                " and forward with " + protocol + " - " + serviceHost + ":" + servicePort);
    }

    public void createProxy(Proxy proxy) {
        reporter.debug("send to managementServer: create proxy " + proxy);
        ProxyResult proxyResult = sendCreateProxyMsgAndGetResponse(proxy.getProtocol(), proxy.getPort());
        if (proxyResult == null || proxyResult.getType() != TypeConstant.SUCCESS) {
            throw new RomaException("create proxy " + proxy + " failed");
        }
        createForwardClient(getHost(), proxyResult.getForwardServerPort(), proxy.getProtocol(),
                proxy.getServiceHost(), proxy.getServicePort());
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PONG) {
                reporter.debug("receive from managementServer: pong");
            } else if (tlv.getType() == TypeConstant.SUCCESS || tlv.getType() == TypeConstant.FAILED) {
                SocketFuture<ProxyResult> future = requestFuture.remove(tlv.getValue().readInt());
                if (future != null) {
                    future.set(new ProxyResult(tlv.getType(), tlv.getValue().readUnsignedShort()));
                }
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                reporter.debug("send to managementServer: ping");
                ctx.channel().writeAndFlush(new Tlv(TypeConstant.PING));
            } else if (state == IdleState.READER_IDLE) {
                reporter.error("disconnect because of reader time out");
                ctx.channel().close();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            reporter.error("managementServer disconnect");
            super.channelInactive(ctx);
        }
    }

}
