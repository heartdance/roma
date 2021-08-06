package com.hidebush.roma.util.entity;

/**
 * Created by htf on 2021/8/4.
 */
public class Tlv {

    private int type;

    private int length;

    private byte[] value;

    public Tlv() {
    }

    public Tlv(int type, int length, byte[] value) {
        this.type = type;
        this.length = length;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
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
