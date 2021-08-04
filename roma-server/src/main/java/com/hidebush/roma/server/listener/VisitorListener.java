package com.hidebush.roma.server.listener;

import com.hidebush.roma.server.role.Client;
import com.hidebush.roma.server.role.Receiver;
import com.hidebush.roma.server.role.Visitor;
import com.hidebush.roma.util.network.Forwarder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by htf on 2020/9/28.
 */
public class VisitorListener implements PortListener {

    private final Client client;
    private ServerSocket mappingSocket;

    public VisitorListener(Client client) {
        this.client = client;
    }

    @Override
    public void listen(int port) {
        try {
            mappingSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("建立受访代理套接字（端口：" + port + "）失败：" + e.getMessage());
            return;
        }
        while (true) {
            Socket visitorSocket;
            try {
                visitorSocket = mappingSocket.accept();
            } catch (IOException e) {
                System.out.println("监听受访代理端口" + port + "失败：" + e.getMessage());
                return;
            }
            new Thread(() -> {
                Visitor visitor = new Visitor(visitorSocket);
                Receiver receiver;
                try {
                    receiver = client.createReceiver();
                } catch (IOException e) {
                    System.out.println("客户端无法与代理配对：" + e.getMessage());
                    return;
                }
                new Forwarder(visitor, receiver).forward();
            }).start();
        }
    }

    public void close() {
        if (mappingSocket != null) {
            try {
                mappingSocket.close();
            } catch (IOException ignored) {

            }
        }
    }
}
