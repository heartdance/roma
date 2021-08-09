package com.hidebush.roma.client;

import com.hidebush.roma.util.Bytes;
import com.hidebush.roma.util.config.TypeConstant;
import com.hidebush.roma.util.entity.Tlv;
import com.hidebush.roma.util.network.TcpClient;
import com.hidebush.roma.util.network.TlvDecoder;
import com.hidebush.roma.util.network.TlvEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 连接服务端，转发服务端消息到 {@link ServiceClient}
 * Created by htf on 2021/8/6.
 */
public class ForwardClient extends TcpClient {

    private final String serviceHost;
    private final int servicePort;

    private final ConcurrentMap<Integer, ServiceClient> serviceClientMap = new ConcurrentHashMap<>();

    public ForwardClient(String host, int port, String serviceHost, int servicePort) {
        super(host, port);
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 5, 2))
                .addLast(new TlvEncoder(4, 1, 2))
                .addLast(new TlvDecoder(4, 1, 2))
                .addLast(new TlvHandler());
    }

    private void createServiceClient(int id) {
        ServiceClient serviceClient = new ServiceClient(id, serviceHost, servicePort, this);
        serviceClientMap.put(id, serviceClient);
        serviceClient.startup();
    }

    public void sendMsgToForwardServer(int serviceClientId, byte[] data) {
        Tlv tlv = new Tlv(TypeConstant.ON_SERVICE_SEND_MSG,
                Bytes.merge(Bytes.toBytes(serviceClientId, 4), data));
        getChannel().writeAndFlush(tlv);
    }

    private class TlvHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Tlv tlv = (Tlv) msg;
            if (tlv.getType() == TypeConstant.ON_VISITOR_CONNECT) {
                int visitorId = Bytes.toInt(tlv.getValue());
                createServiceClient(visitorId);
            } else if (tlv.getType() == TypeConstant.ON_VISITOR_SEND_MSG) {
                int visitorId = Bytes.toInt(tlv.getValue(), 0, 4);
                ServiceClient serviceClient = serviceClientMap.get(visitorId);
                if (serviceClient != null) {
                    byte[] value = tlv.getValue();
                    byte[] bytes = new byte[value.length - 4];
                    System.arraycopy(value, 4, bytes, 0, bytes.length);
                    serviceClient.sendMsgToService(bytes);
                }
            }
        }
    }

}
