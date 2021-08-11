package com.hidebush.roma.client;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.SocketFuture;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.exception.RomaException;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理本地代理
 * Created by htf on 2021/8/6.
 */
public class ManagementClient extends TcpClient {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final AtomicInteger requestId = new AtomicInteger();

    private final ConcurrentMap<Integer, SocketFuture<Tlv>> requestFuture = new ConcurrentHashMap<>();

    public ManagementClient(String host, int port) {
        super(host, port);
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ManagementClient(" + id + ")");
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

    private Tlv sendToManagementServer(Tlv tlv) {
        int id = requestId.incrementAndGet();
        SocketFuture<Tlv> future = new SocketFuture<>();
        requestFuture.put(id, future);
        tlv.setId(id);
        getChannel().writeAndFlush(tlv);
        try {
            return future.get();
        } catch (InterruptedException e) {
            return null;
        }
    }

    private void createForwardClient(String host, int port, String serviceHost, int servicePort) {
        ForwardClient forwardClient = new ForwardClient(host, port, serviceHost, servicePort);
        forwardClient.startup();
        reporter.info("forwardClient(" + forwardClient.id() + ") startup for proxy " +
                serviceHost + ":" + servicePort + " -> " + getHost() + ":" + port);
    }

    public void createProxy(int port, String serviceHost, int servicePort) {
        reporter.debug("start create proxy " + serviceHost + ":" + servicePort + " -> " + getHost() + ":" + port);
        Tlv tlv = sendToManagementServer(new Tlv(TypeConstant.CREATE_PROXY, Bytes.toBytes(port, 2)));
        if (tlv == null || tlv.getType() != TypeConstant.SUCCESS) {
            throw new RomaException("create proxy " + serviceHost + ":" + servicePort + " -> " + port + " failed");
        }
        int forwardPort = Bytes.toInt(tlv.getValue());
        createForwardClient(getHost(), forwardPort, serviceHost, servicePort);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.PONG) {
                reporter.debug("receive from managementServer: pong");
            } else {
                SocketFuture<Tlv> future = requestFuture.remove(tlv.getId());
                if (future != null) {
                    future.set(tlv);
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            reporter.error("managementServer disconnect");
            super.channelInactive(ctx);
        }
    }

}
