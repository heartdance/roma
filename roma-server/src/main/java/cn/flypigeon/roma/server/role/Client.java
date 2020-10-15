package cn.flypigeon.roma.server.role;

import cn.flypigeon.roma.server.listener.ClientListener;
import cn.flypigeon.roma.util.network.SocketAdaptor;
import cn.flypigeon.roma.util.config.Command;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by htf on 2020/9/28.
 */
public class Client extends SocketAdaptor {

    public Client(Socket socket) {
        super(socket);
    }

    public Receiver createReceiver() throws IOException {
        send(Command.CREATE_RECEIVER);
        int id = receiveInt();
        return ClientListener.INSTANCE.getReceiver(id);
    }

}
