package com.hidebush.roma.util.entity;

import com.hidebush.roma.util.Bytes;

/**
 * Created by htf on 2021/8/4.
 */
public class Tlv {

    private int type;

    private byte[] value;

    public Tlv(int type) {
        this.type = type;
    }

    public Tlv(int type, byte[] value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public int getLength() {
        return value == null ? 0 : value.length;
    }

    @Override
    public String toString() {
        return "Tlv{" +
                "type=" + type +
                ", length=" + getLength() +
                ", value=" + Bytes.toHex(value) +
                '}';
    }
}
