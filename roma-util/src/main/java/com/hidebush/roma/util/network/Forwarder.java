package com.hidebush.roma.util.network;

import java.io.IOException;

/**
 * Created by htf on 2020/9/28.
 */
public class Forwarder {

    private final SocketAdaptor visitor;
    private final SocketAdaptor receiver;

    public Forwarder(SocketAdaptor visitor, SocketAdaptor receiver) {
        this.visitor = visitor;
        this.receiver = receiver;
    }

    public void forward() {
        new Thread(() -> this.forward(this.visitor, this.receiver)).start();
        new Thread(() -> this.forward(this.receiver, this.visitor)).start();
    }

    private void forward(SocketAdaptor from, SocketAdaptor to) {
        try {
            ByteBuf byteBuf = new ByteBuf(1024);
            while (true) {
                from.receive(byteBuf);
                if (byteBuf.getLen() > 0) {
                    to.send(byteBuf);
                    byteBuf.setLen(0);
                } else if (byteBuf.getLen() == -1) {
                    from.close();
                    to.close();
                    break;
                }
            }
        } catch (IOException e) {
            from.close();
            to.close();
        }
    }
}
