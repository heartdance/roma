package cn.flypigeon.roma.client.role;

import cn.flypigeon.roma.util.network.SocketAdaptor;
import cn.flypigeon.roma.util.config.ClientType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by htf on 2020/9/28.
 */
public class Client extends SocketAdaptor {

    public Client(Socket socket) {
        super(socket);
    }

    public void sendType() throws IOException {
        send(ClientType.CLIENT);
    }

    public void sendId(int id) throws IOException {
        sendInt(id);
    }

    public int readPort() throws IOException {
        return receiveInt();
    }

    public int readCommand() throws IOException {
        return receive();
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
