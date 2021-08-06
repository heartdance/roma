package com.hidebush.roma.client;

import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by htf on 2021/8/6.
 */
public class ManagementClient extends TcpClient {

    public ManagementClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 1, 2))
                .addLast(new TlvEncoder(1, 2))
                .addLast(new TlvDecoder(1, 2));
    }

    private void createForwardClient(String host, int port) {
        ForwardClient forwardClient = new ForwardClient(host, port);
    }

    public void sendToManagementServer(Tlv tlv) {
        getChannel().writeAndFlush(tlv);
    }
}
