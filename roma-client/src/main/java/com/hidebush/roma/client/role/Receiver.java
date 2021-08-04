package com.hidebush.roma.client.role;

import com.hidebush.roma.util.network.SocketAdaptor;
import com.hidebush.roma.util.config.ClientType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by htf on 2020/9/28.
 */
public class Receiver extends SocketAdaptor {

    public Receiver(Socket socket) {
        super(socket);
    }

    public void sendType() throws IOException {
        send(ClientType.RECEIVER);
    }

    public int receivedId() throws IOException {
        return receiveInt();
    }
}
