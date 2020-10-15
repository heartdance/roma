package cn.flypigeon.roma.util.network;

import cn.flypigeon.roma.util.Bytes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by htf on 2020/9/28.
 */
public class SocketAdaptor {

    protected Socket socket;

    public SocketAdaptor(Socket socket) {
        this.socket = socket;
    }

    public void send(int b) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(b);
        outputStream.flush();
    }

    public void sendInt(int i) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(Bytes.int2Bytes(i));
        outputStream.flush();
    }

    public void send(ByteBuf byteBuf) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(byteBuf.getBytes(), 0, byteBuf.getLen());
        outputStream.flush();
    }

    public int receive() throws IOException {
        return socket.getInputStream().read();
    }

    public int receiveInt() throws IOException {
        byte[] bytes = new byte[4];
        int read = socket.getInputStream().read(bytes);
        if (read == -1) {
            throw new IOException();
        }
        return Bytes.bytes2Int(bytes);
    }

    public void receive(ByteBuf byteBuf) throws IOException {
        int read = socket.getInputStream().read(byteBuf.getBytes());
        if (read == -1) {
            throw new IOException();
        }
        byteBuf.setLen(read);
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
