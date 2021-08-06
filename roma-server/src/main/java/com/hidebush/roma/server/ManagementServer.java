package com.hidebush.roma.server;

import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by htf on 2021/8/5.
 */
public class ManagementServer extends TcpServer {

    public ManagementServer(int localPort) {
        super(localPort);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 1, 2))
                .addLast(new TlvEncoder(1, 2))
                .addLast(new TlvDecoder(1, 2));
    }

    public void sendToManagementClient(Tlv tlv) {
        getChannel().writeAndFlush(tlv);
    }

    private void createVisitorServer(int port) {
        VisitorServer visitorServer = new VisitorServer(port);
    }

}
