package cn.edu.ncu.core;

import cn.edu.ncu.common.SerializableUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import cn.edu.ncu.domain.Message;
import cn.edu.ncu.domain.ServerSessionStatus;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import static cn.edu.ncu.common.RedisKey.*;

public class SendMessageService implements Runnable{

    private final Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    private Channel channel;
    private Message message;
    private Jedis jedis;
    private SerializableUtil serializableUtil = new SerializableUtil();

    private final int REPEAT = 5;
//    初始重试等待的时间
    private final int INIT_WAIT_TIME = 1000;
//    每经过一次重试后下一次重试增加的时间
    private final int INCREASED_WAIT_TIME = 500;

    private void sendMessage(Message message, Channel channel){
        switch (MqttQoS.valueOf(message.getQoS())){
            case AT_MOST_ONCE:
                sendAtMostOnce(message, channel);
                break;
            case AT_LEAST_ONCE:
                sendAtLeastOnce(message, channel);
                break;
            case EXACTLY_ONCE:
                sendExactlyOnce(message, channel);
                break;
            default:
                break;
        }
    }


    private void sendExactlyOnce(Message message, Channel channel) {

    }

    private void sendAtLeastOnce(Message message, Channel channel) {
        System.out.println("sendAtLeastOnce");
        String messageId = String.valueOf(message.getPackageId());
        String key = String.format(UNACK_MESSAGE_KEY_MODEL, messageId);

//        加上5000ms用于IO等处理的延迟
        long maxWaitTime = INIT_WAIT_TIME+((INCREASED_WAIT_TIME*(REPEAT-2))*(REPEAT-1)/2)+5000;

        jedis.set(key.getBytes(), serializableUtil.encode(message), "NX".getBytes(),
                "PX".getBytes(), maxWaitTime);

        for (int i = 0; i<REPEAT; ++i){
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes == null){
                System.out.println("not exists key");
                logger.warn("not exists key: %s at repeat%d", key, i);
                break;
            }
            Message tmp = (Message) serializableUtil.decode(bytes);
            if (tmp.getStatus() == MqttMessageType.PUBACK){
                System.out.println("already puback");
                break;
            }
            sendAtMostOnce(message, channel);
            try {
                Thread.sleep(INIT_WAIT_TIME + INCREASED_WAIT_TIME*i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] bytes = jedis.get(key.getBytes());
        if (bytes == null){
            logger.warn("not exists key: %s", key);
            return;
        }
        Message tmp = (Message) serializableUtil.decode(bytes);


        if (tmp.getStatus() != MqttMessageType.PUBACK){
//            取出status
            String clientId = jedis.get(channel.id().asLongText());
            if (clientId == null){
                logger.error("key %s not exists", channel.id().asLongText());
                return;
            }
            String statusKey = String.format(CLIENT_KEY_MODEL, clientId);
            byte[] statusBytes = jedis.get(statusKey.getBytes());
            if (statusBytes == null){
                logger.error("key %s not exists", statusKey);
                return;
            }
            ServerSessionStatus status = (ServerSessionStatus) serializableUtil.decode(statusBytes);


            logger.info("message not ack: topic %s, channelId %s",
                    message.getTopicName(), message.getChannelId().asLongText());
            status.getUnAckMessages().add(message);
            jedis.set(statusKey.getBytes(), serializableUtil.encode(status));
        }
        jedis.del(key.getBytes());
    }

    private void sendAtMostOnce(Message message, Channel channel) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH,
                message.isDup(), MqttQoS.valueOf(message.getQoS()), message.isRetain(),
                message.getRemainingLength());
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(message.getTopicName(), message.getPackageId());
        ByteBuf byteBuf = Unpooled.directBuffer();
        byteBuf.writeBytes(message.getContent());
        MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, variableHeader, byteBuf);
        channel.writeAndFlush(publishMessage);
    }

    @Override
    public void run() {
        sendMessage(message, channel);
        jedis.close();
    }

    public SendMessageService(Channel channel, Message message, Jedis jedis) {
        this.channel = channel;
        this.message = message;
        this.jedis = jedis;
    }
}
