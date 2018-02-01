package cn.edu.ncu;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.mqtt.*;

import java.net.SocketAddress;

public class TestHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {

        System.out.println("connect successful");

        MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader("MQTT",
                MqttVersion.MQTT_3_1_1.protocolLevel(),
                false, false,
                false, 0, false,
                true, 10000);
        MqttConnectPayload payload = new MqttConnectPayload("1", "", "".getBytes(), "", "".getBytes());
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 2);
        MqttConnectMessage connectMessage = new MqttConnectMessage(fixedHeader, variableHeader, payload);
        ctx.writeAndFlush(connectMessage);
    }
}
