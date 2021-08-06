package com.hidebush.roma.client;

import com.hidebush.roma.util.network.TcpClient;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by htf on 2021/8/6.
 */
public class ForwardClient extends TcpClient {

    public ForwardClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void initChannel(SocketChannel ch) {

    }

    private void createServiceClient(String host, int port) {
        ServiceClient serviceClient = new ServiceClient(host, port);
    }

    public void sendMsgToForwardServer(int serviceClientId, byte[] data) {

    }
}
