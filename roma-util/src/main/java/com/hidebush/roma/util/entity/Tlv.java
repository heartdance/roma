package com.hidebush.roma.util.entity;

import io.netty.buffer.ByteBuf;

/**
 * Created by htf on 2021/8/4.
 */
public class Tlv {

    private final int type;

    private final ByteBuf value;

    public Tlv(int type) {
        this(type, null);
    }

    public Tlv(int type, ByteBuf value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public ByteBuf getValue() {
        return value;
    }

    public int getLength() {
        return value == null ? 0 : value.readableBytes();
    }

    @Override
    public String toString() {
        return "Tlv{" +
                "type=" + type +
                ", length=" + getLength() +
                ", value=" + value +
                '}';
    }
}
