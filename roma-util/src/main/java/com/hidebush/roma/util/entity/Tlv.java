package com.hidebush.roma.util.entity;

/**
 * Created by htf on 2021/8/4.
 */
public class Tlv {

    private int id;

    private int type;

    private byte[] value;

    public Tlv(int id, int type, byte[] value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public Tlv(int type, byte[] value) {
        this.type = type;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return value.length;
    }
}
