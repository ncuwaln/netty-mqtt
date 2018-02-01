package cn.edu.ncu.core;


import static cn.edu.ncu.common.RedisKey.*;
import cn.edu.ncu.common.RedisPoolUtil;
import cn.edu.ncu.common.SerializableUtil;
import cn.edu.ncu.domain.*;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class MqttHandler extends ChannelInboundHandlerAdapter {
    private final byte protocolLevel = 0x04;
    private final String protocolName = "MQTT";
    public static ChannelGroup channelGroup = new DefaultChannelGroup("mqtt_channel_group", null);

    private SerializableUtil serializableUtil = new SerializableUtil();

    private final Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage message = (MqttMessage) msg;

        switch (message.fixedHeader().messageType()){
            case CONNECT:
                MqttConnAckMessage ackMessage = onConnect((MqttConnectMessage) message, ctx);
                ctx.writeAndFlush(ackMessage);
                break;
            case SUBSCRIBE:
                MqttSubAckMessage subAckMessage = onSubscribe((MqttSubscribeMessage)message, ctx);
                ctx.writeAndFlush(subAckMessage);
                break;
            case UNSUBSCRIBE:
                MqttUnsubAckMessage unsubAckMessage = onUnSubscribe((MqttUnsubscribeMessage)message, ctx);
                ctx.writeAndFlush(unsubAckMessage);
                break;
            case PINGREQ:
                MqttMessage pingResp = onPing();
                ctx.writeAndFlush(pingResp);
                break;
            case PUBLISH:
                MqttPubAckMessage pubAckMessage = onPublish((MqttPublishMessage)message, ctx);
                ctx.writeAndFlush(pubAckMessage);
                break;
            case PUBACK:
                onPuback((MqttPubAckMessage)message, ctx);
                break;
            case DISCONNECT:
                onDisconnect(ctx);
            default:
                System.out.println("not this message");
                break;
        }
    }

    private void onDisconnect(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void onPuback(MqttPubAckMessage message, ChannelHandlerContext ctx) {
        Jedis jedis = RedisPoolUtil.getConn();
        int messageId = message.variableHeader().messageId();
        String key = String.format(UNACK_MESSAGE_KEY_MODEL, messageId);
        byte[] bytes = jedis.get(key.getBytes());
        if (bytes == null) return;
        Message tmp = (Message) serializableUtil.decode(bytes);
        tmp.setStatus(MqttMessageType.PUBACK);
        bytes = serializableUtil.encode(tmp);
        jedis.set(key.getBytes(), bytes);
        jedis.close();
    }

    private MqttPubAckMessage onPublish(MqttPublishMessage message, ChannelHandlerContext ctx) {
        MqttPublishVariableHeader variableHeader = message.variableHeader();
        MqttFixedHeader fixedHeader = message.fixedHeader();
        String topicName = variableHeader.topicName();
        Jedis jedis = RedisPoolUtil.getConn();

        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK,
                false, MqttQoS.AT_LEAST_ONCE, false, 2);
        MqttMessageIdVariableHeader messageIdVariableHeader = MqttMessageIdVariableHeader.from(message.variableHeader().packetId());
        MqttPubAckMessage pubAckMessage = new MqttPubAckMessage(ackFixedHeader, messageIdVariableHeader);

        String key = String.format(TOPIC_KEY_MODEL, topicName);
        byte[] bytes = jedis.get(key.getBytes());

        if (bytes == null){
            jedis.close();
            return pubAckMessage;
        }
        TopicSub topicSub = (TopicSub) serializableUtil.decode(bytes);

        Set<Sub> subs = topicSub.getSubs();
        Iterator<Sub> iterator = subs.iterator();

        while (iterator.hasNext()){

            Sub sub = iterator.next();

            byte[] content = new byte[message.content().readableBytes()];
            message.content().getBytes(0, content);

            Message sendMessage = new Message();
            sendMessage.setTopicName(topicName);
            sendMessage.setChannelId(sub.getChannelId());
            sendMessage.setPackageId(message.variableHeader().packetId());
            sendMessage.setContent(content);
            sendMessage.setStatus(MqttMessageType.PUBLISH);
            sendMessage.setQoS(message.fixedHeader().qosLevel().value());
            sendMessage.setDup(fixedHeader.isDup());
            sendMessage.setRemainingLength(fixedHeader.remainingLength());
            sendMessage.setRetain(fixedHeader.isRetain());

            jedis.rpush(PREPARE_SEND_MESSAGE_KEY.getBytes(), serializableUtil.encode(sendMessage));
        }
        jedis.close();
        return pubAckMessage;
    }

    private MqttMessage onPing() {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        return new MqttMessage(mqttFixedHeader);
    }

    private MqttUnsubAckMessage onUnSubscribe(MqttUnsubscribeMessage message, ChannelHandlerContext ctx) {
        List<String> topics = message.payload().topics();
        Channel ch = ctx.channel();
        Jedis jedis = RedisPoolUtil.getConn();
        Pipeline pipeline = jedis.pipelined();

        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK,
                false, MqttQoS.AT_MOST_ONCE, false, 2);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(message.variableHeader().messageId());
        MqttUnsubAckMessage unsubAckMessage = new MqttUnsubAckMessage(fixedHeader, variableHeader);

//        在存储状态中删除要订阅的topic
        String clientId = jedis.get(ch.id().asLongText());
        String status_key = String.format(CLIENT_STATUS_KEY_MODEL, clientId);
        byte[] statusBytes = jedis.get(status_key.getBytes());
        if (statusBytes == null){
            jedis.close();
            return unsubAckMessage;
        }
        ServerSessionStatus status = (ServerSessionStatus) serializableUtil.decode(statusBytes);
        status.unSubscribe(topics.toArray(new String[0]));

//        在topic中删除channel
        for (String topic: topics){
            String key = String.format(TOPIC_KEY_MODEL, topic);
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes == null){
                continue;
            }
            TopicSub topicSub = (TopicSub) serializableUtil.decode(bytes);

            topicSub.ubSubscribe(ch.id().asLongText());

            pipeline.set(key.getBytes(), serializableUtil.encode(topicSub));

//            pipeline.set(status_key.getBytes(), serializableUtil.encode(status));
//            TopicSub topicSubs = topicSubMap.get(topic);
        }
        pipeline.set(status_key.getBytes(), serializableUtil.encode(status));

        pipeline.syncAndReturnAll();
        jedis.close();

        return unsubAckMessage;
    }

    private MqttSubAckMessage onSubscribe(MqttSubscribeMessage message, ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        MqttSubscribePayload mqttSubscribePayload = message.payload();
        List<MqttTopicSubscription> subscriptions = mqttSubscribePayload.topicSubscriptions();
        List<Integer> qosList = new LinkedList<Integer>();

        Jedis jedis = RedisPoolUtil.getConn();

        for (MqttTopicSubscription topicSubscription: subscriptions){
            System.out.println(topicSubscription.topicName());
            String key = String.format(TOPIC_KEY_MODEL, topicSubscription.topicName());
            byte[] bytes = jedis.get(key.getBytes());
            TopicSub topicSub = null;
            if (bytes == null){
                topicSub = new TopicSub();
            }else {
                topicSub = (TopicSub) serializableUtil.decode(bytes);
            }
//            TopicSub topicSub = topicSubMap.get(topicSubscription.topicName());
//            if (topicSub == null){
//                topicSub = new TopicSub();
//            }
            System.out.println("init sub");
            Sub sub = new Sub();
            sub.setQos(topicSubscription.qualityOfService().value());
            sub.setChannelId(ch.id());
            sub.setTopicName(topicSubscription.topicName());
            if (topicSub.getSubs().contains(sub))
                continue;

            System.out.println("设置topic");
            topicSub.setTopicName(topicSubscription.topicName());
            qosList.add(topicSubscription.qualityOfService().value());
            topicSub.getSubs().add(sub);
            jedis.set(key.getBytes(), serializableUtil.encode(topicSub));

//            存储客户端的client的订阅信息
            System.out.println("存储客户端的client的订阅信息");
            String clientKey = String.format(CHANNEL_CLIENT_KEY_MODEL, ch.id().asLongText());
            System.out.println(clientKey);
            String clientId = jedis.get(clientKey);
            String status_key = String.format(CLIENT_STATUS_KEY_MODEL, clientId);
            byte[] statusBytes = jedis.get(status_key.getBytes());
            ServerSessionStatus status = (ServerSessionStatus) serializableUtil.decode(statusBytes);
            status.getSubscriptions().add(sub);
            jedis.set(status_key.getBytes(), serializableUtil.encode(status));

//            topicSubMap.put(topicSubscription.topicName(), topicSub);
        }

        System.out.println("构造返回信息");
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK,
                false, MqttQoS.AT_MOST_ONCE, false, 2);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(message.variableHeader().messageId());
        MqttSubAckPayload mqttSubAckPayload = new MqttSubAckPayload(qosList);

        System.out.println("return redis connection");
        jedis.close();

        return new MqttSubAckMessage(fixedHeader, variableHeader, mqttSubAckPayload);
    }


    private MqttConnAckMessage onConnect(MqttConnectMessage mqttMessage, ChannelHandlerContext channelHandlerContext) {

        MqttConnectVariableHeader variableHeader = mqttMessage.variableHeader();
        String clientIdentifier = mqttMessage.payload().clientIdentifier();
        Channel ch = channelHandlerContext.channel();

        System.out.println("保存channel");
//        保存channel
        channelGroup.add(ch);

        System.out.println("设置心跳");
//        设置心跳
        int keepAlive = variableHeader.keepAliveTimeSeconds();
        channelHandlerContext.pipeline().addBefore(
                "MqttServerHandler",
                "MqttIdleHandler",
                new IdleStateHandler(keepAlive, 0, 0));

        System.out.println("构造client信息");
//        构造client信息
        ClientInfo client = new ClientInfo();
        client.setChannelId(ch.id());
        client.setClientIdentifier(clientIdentifier);
        client.setKeepAlive(keepAlive);

        System.out.println("存储客户端信息");
//        存储客户端信息以及channelId与clientId的对应信息
        Jedis jedis = RedisPoolUtil.getConn();
        String clientKey = String.format(CLIENT_KEY_MODEL, clientIdentifier);
        String channelClientKey = String.format(CHANNEL_CLIENT_KEY_MODEL, ch.id().asLongText());

        System.out.println(clientKey);
        System.out.println(channelClientKey);

        jedis.set(clientKey.getBytes(),
                serializableUtil.encode(client));
        jedis.set(channelClientKey.getBytes(),
                clientIdentifier.getBytes());



//        clientInfoMap.put(clientIdentifier, client);
//        channelClientMap.put(ch.id(), clientIdentifier);

//        处理clean session
        System.out.println("处理cleanSession");
        boolean sessionPresent = false;

        String key = String.format(CLIENT_STATUS_KEY_MODEL, clientIdentifier);
        if (!variableHeader.isCleanSession()){
            sessionPresent = true;
            if (jedis.get(key.getBytes()) == null){
                sessionPresent = false;
                jedis.set(key.getBytes(), serializableUtil.encode(new ServerSessionStatus()));
//                sessionStatusMap.put(clientIdentifier, new ServerSessionStatus());
            }
        }else {
            System.out.println("构造serverSessionStatus");
            jedis.set(key.getBytes(), serializableUtil.encode(new ServerSessionStatus()));
//            sessionStatusMap.put(clientIdentifier, new ServerSessionStatus());
        }


        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false,
                MqttQoS.FAILURE, false, 0x02);
        MqttConnAckVariableHeader connAckVariableHeader = new MqttConnAckVariableHeader(
                MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent);

        System.out.println("归还redis链接");
//        关闭redis连接,实际是归还redis连接
        jedis.close();

        return new MqttConnAckMessage(fixedHeader, connAckVariableHeader);
    }
}