package cn.flypigeon.roma.util.network;

/**
 * Created by htf on 2020/9/28.
 */
public class ByteBuf {

    private byte[] bytes;
    private int len;

    public ByteBuf(byte[] bytes, int len) {
        this.bytes = bytes;
        this.len = len;
    }

    public ByteBuf(int size) {
        this.bytes = new byte[size];
    }

    public ByteBuf(byte[] bytes) {
        this.bytes = bytes;
        this.len = bytes.length;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
