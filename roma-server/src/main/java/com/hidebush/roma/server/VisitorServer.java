package com.hidebush.roma.server;

import com.hidebush.roma.util.network.TcpServer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by htf on 2021/8/6.
 */
public class VisitorServer extends TcpServer {

    public VisitorServer(int localPort) {
        super(localPort);
    }

    @Override
    protected void initChannel(SocketChannel ch) {

    }

    private void createForwardServer() {
        ForwardServer forwardServer = new ForwardServer(1);
    }

    public void sendMsgToVisitor(int visitorId, byte[] data) {

    }

}
