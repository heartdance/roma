package com.hidebush.roma.server.role;

import com.hidebush.roma.util.network.SocketAdaptor;

import java.net.Socket;

/**
 * Created by htf on 2020/9/28.
 */
public class Receiver extends SocketAdaptor {

    public Receiver(Socket socket) {
        super(socket);
    }

}