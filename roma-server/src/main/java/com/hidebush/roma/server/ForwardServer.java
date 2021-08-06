package com.hidebush.roma.server;

import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by htf on 2021/8/6.
 */
public class ForwardServer extends TcpServer {

    public ForwardServer(int localPort) {
        super(localPort);
    }

    @Override
    protected void initChannel(SocketChannel ch) {

    }

    public void sendMsgToClient(int visitorId, byte[] data) {

    }

}
