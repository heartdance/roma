package com.hidebush.roma.util.entity;

/**
 * Created by htf on 2021/8/4.
 */
public class Tlv {

    private byte type;

    private int length;

    private byte[] value;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
