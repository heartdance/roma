package cn.flypigeon.roma.server.listener;

import cn.flypigeon.roma.server.manager.PortManager;
import cn.flypigeon.roma.server.manager.SimplePortManager;
import cn.flypigeon.roma.server.role.Client;
import cn.flypigeon.roma.server.role.Receiver;
import cn.flypigeon.roma.util.config.ClientType;
import cn.flypigeon.roma.util.network.SocketAdaptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by htf on 2020/9/28.
 */
public class ClientListener implements PortListener {

    public static final ClientListener INSTANCE = new ClientListener();

    private static final PortManager portManager = new SimplePortManager();
    private final AtomicInteger receiverId = new AtomicInteger(0);
    private final Map<Integer, Receiver> receiverMap = new ConcurrentHashMap<>();

    private ClientListener() {

    }

    public Receiver getReceiver(int id) {
        return receiverMap.remove(id);
    }

    @Override
    public void listen(int port) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("主程序启动失败，请检查端口" + port + "是否被占用");
            e.printStackTrace();
            return;
        }
        while (true) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("主程序监听连接发生异常：" + e.getMessage());
                break;
            }
            new Thread(() -> {
                try {
                    Receiver receiver = new Receiver(clientSocket);
                    int type = receiver.receive();
                    if (type == ClientType.RECEIVER) {
                        int id = receiverId.getAndIncrement();
                        receiverMap.put(id, receiver);
                        receiver.sendInt(id);
                        return;
                    }
                } catch (IOException e) {
                    System.out.println("与客户端通信发生异常：" + e.getMessage());
                    return;
                }
                Client client = new Client(clientSocket);
                int freePort = portManager.getFreePort();
                VisitorListener visitorListener = new VisitorListener(client);
                try {
                    client.sendInt(freePort);
                } catch (IOException e) {
                    System.out.println("与客户端通信发生异常：" + e.getMessage());
                    e.printStackTrace();
                    client.close();
                    visitorListener.close();
                    return;
                }
                visitorListener.listen(freePort);
            }).start();
        }
    }
}
