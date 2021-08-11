package com.hidebush.roma.client;

import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.reporter.Reporter;
import com.hidebush.roma.util.reporter.ReporterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接服务，将服务发出的消息发送到 {@link ForwardClient}
 * Created by htf on 2021/8/6.
 */
public class ServiceClient extends TcpClient {

    private static final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final Reporter reporter;

    private final int visitorId;
    private final ForwardClient forwardClient;

    public ServiceClient(int visitorId, String host, int port, ForwardClient forwardClient) {
        super(host, port);
        this.visitorId = visitorId;
        this.forwardClient = forwardClient;
        this.id = ids.incrementAndGet();
        this.reporter = ReporterFactory.createReporter("ServiceClient(" + id + ")");
    }

    public int id() {
        return id;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new ServiceHandler());
    }

    public void sendMsgToService(byte[] data) {
        reporter.debug("send to service " + data.length + " bytes");
        ByteBuf out = getChannel().alloc().ioBuffer(data.length);
        out.writeBytes(data);
        getChannel().writeAndFlush(data);
    }

    private class ServiceHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            if (in.isReadable()) {
                byte[] bytes = new byte[in.readableBytes()];
                reporter.debug("receive from service " + bytes.length + " bytes");
                in.readBytes(bytes);
                forwardClient.sendMsgToForwardServer(visitorId, bytes);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            reporter.debug("service disconnect");
            forwardClient.sendDisconnectMsgToForwardClient(visitorId);
        }
    }
}
