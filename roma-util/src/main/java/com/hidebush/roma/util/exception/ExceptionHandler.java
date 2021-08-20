package com.hidebush.roma.util.exception;

import com.hidebush.roma.util.reporter.Reporter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by htf on 2021/8/20.
 */
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    
    private final Reporter reporter;

    public ExceptionHandler(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        reporter.error("channel error", cause);
    }
}
