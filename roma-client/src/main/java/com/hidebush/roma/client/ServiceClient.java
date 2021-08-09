package com.hidebush.roma.client;

import com.hidebush.roma.util.network.TcpClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * 连接服务，将服务发出的消息发送到 {@link ForwardClient}
 * Created by htf on 2021/8/6.
 */
public class ServiceClient extends TcpClient {

    private final int id;
    private final ForwardClient forwardClient;

    public ServiceClient(int id, String host, int port, ForwardClient forwardClient) {
        super(host, port);
        this.id = id;
        this.forwardClient = forwardClient;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new ServiceHandler());
    }

    public void sendMsgToService(byte[] data) {
        ByteBuf out = getChannel().alloc().ioBuffer(data.length);
        try {
            out.writeBytes(data);
            getChannel().writeAndFlush(data);
        } finally {
            ReferenceCountUtil.release(out);
        }
    }

    private class ServiceHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            if (in.isReadable()) {
                byte[] bytes = new byte[in.readableBytes()];
                in.readBytes(bytes);
                forwardClient.sendMsgToForwardServer(id, bytes);
            }
        }
    }
}
