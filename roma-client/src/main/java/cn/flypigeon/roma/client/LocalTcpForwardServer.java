package cn.flypigeon.roma.client;

import cn.flypigeon.roma.util.network.Forwarder;
import cn.flypigeon.roma.client.role.Receiver;
import cn.flypigeon.roma.client.role.Client;
import cn.flypigeon.roma.util.network.SocketAdaptor;
import cn.flypigeon.roma.util.config.Command;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by htf on 2020/9/27.
 */
public class LocalTcpForwardServer {

    private final String serverIp;
    private final int serverPort;
    private final String forwardIp;
    private final int forwardPort;

    public LocalTcpForwardServer(String serverIp, int serverPort, String forwardIp, int forwardPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.forwardIp = forwardIp;
        this.forwardPort = forwardPort;
    }

    public void startup() {
        Socket serverSocket;
        try {
            serverSocket = new Socket(serverIp, serverPort);
        } catch (IOException e) {
            Reporter.error("连接服务器失败");
            e.printStackTrace();
            return;
        }
        Client client = new Client(serverSocket);
        int remotePort;
        try {
            client.sendType();
            remotePort = client.readPort();
        } catch (IOException e) {
            Reporter.error("与服务器通信失败");
            e.printStackTrace();
            return;
        }
        Reporter.info("Port mapping created success: " +
                serverIp + ":" + remotePort + " -> " + forwardIp + ":" + forwardPort);

        new Thread(() -> {
            while (true) {
                int command;
                try {
                    command = client.readCommand();
                } catch (IOException e) {
                    Reporter.error("与服务器通信失败");
                    return;
                }
                if (command == Command.CREATE_RECEIVER) {
                    Receiver receiver;
                    try {
                        Socket receiveSocket = new Socket(serverIp, serverPort);
                        receiver = new Receiver(receiveSocket);
                        receiver.sendType();
                        int id = receiver.receivedId();
                        client.sendId(id);
                    } catch (IOException e) {
                        Reporter.error("与服务器建立代理通道失败");
                        return;
                    }
                    Socket serviceSocket;
                    try {
                        serviceSocket = new Socket(forwardIp, forwardPort);
                    } catch (IOException e) {
                        Reporter.error("与服务" + forwardIp + ":" + forwardPort + "建立连接失败");
                        return;
                    }
                    new Forwarder(receiver, new SocketAdaptor(serviceSocket)).forward();
                }
            }
        }).start();
    }
}
