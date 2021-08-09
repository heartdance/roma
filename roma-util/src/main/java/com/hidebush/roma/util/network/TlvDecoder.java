package com.hidebush.roma.util.network;

import com.hidebush.roma.util.entity.Tlv;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by htf on 2021/8/5.
 */
public class TlvDecoder extends ChannelInboundHandlerAdapter {

    private final int idFieldLength;
    private final int typeFieldLength;
    private final int lengthFieldLength;

    public TlvDecoder(int idFieldLength, int typeFieldLength, int lengthFieldLength) {
        this.idFieldLength = idFieldLength;
        this.typeFieldLength = typeFieldLength;
        this.lengthFieldLength = lengthFieldLength;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        int id = readInt(in, idFieldLength);
        int type = readInt(in, typeFieldLength);
        int length = readInt(in, lengthFieldLength);
        if (length < 0) {
            throw new IllegalArgumentException();
        }
        byte[] bytes = new byte[length];
        if (length > 0) {
            in.readBytes(bytes);
        }
        ctx.fireChannelRead(new Tlv(id, type, bytes));
    }
    
    private int readInt(ByteBuf in, int length) {
        switch (length) {
            case 1:
                return in.readUnsignedByte();
            case 2:
                return in.readUnsignedShort();
            case 3:
                return in.readUnsignedMedium();
            case 4:
                return in.readInt();
            default:
                throw new IllegalArgumentException();
        }
    }
}
