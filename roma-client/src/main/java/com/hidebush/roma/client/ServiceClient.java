package com.hidebush.roma.client;

import com.hidebush.roma.util.network.TcpClient;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by htf on 2021/8/6.
 */
public class ServiceClient extends TcpClient {

    public ServiceClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void initChannel(SocketChannel ch) {

    }

    public void sendMsgToService(byte[] data) {

    }
}
