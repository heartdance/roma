package com.hidebush.roma.util.network;

import com.hidebush.roma.util.entity.Tlv;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by htf on 2021/8/6.
 */
public class TlvEncoder extends ChannelOutboundHandlerAdapter {

    private final int idFieldLength;
    private final int typeFieldLength;
    private final int lengthFieldLength;

    public TlvEncoder(int idFieldLength, int typeFieldLength, int lengthFieldLength) {
        this.idFieldLength = idFieldLength;
        this.typeFieldLength = typeFieldLength;
        this.lengthFieldLength = lengthFieldLength;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof Tlv) {
            Tlv tlv = (Tlv) msg;
            ByteBuf out = ctx.alloc().ioBuffer();
            try {
                writeInt(out, tlv.getId(), idFieldLength);
                writeInt(out, tlv.getType(), typeFieldLength);
                writeInt(out, tlv.getLength(), lengthFieldLength);
                out.writeBytes(tlv.getValue());
                ctx.write(out, promise);
            } finally {
                ReferenceCountUtil.release(out);
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    private void writeInt(ByteBuf out, int value, int length) {
        switch (length) {
            case 1:
                out.writeByte(value);
                break;
            case 2:
                out.writeShort(value);
                break;
            case 3:
                out.writeMedium(value);
                break;
            case 4:
                out.writeInt(value);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
}
