package cn.edu.ncu.core;
import cn.edu.ncu.common.RedisPoolUtil;
import cn.edu.ncu.common.SerializableUtil;
import cn.edu.ncu.domain.Message;
import io.netty.channel.Channel;
import redis.clients.jedis.Jedis;

import static cn.edu.ncu.common.Singleton.*;
import static cn.edu.ncu.common.RedisKey.*;

public class Consumer implements Runnable{

    private Jedis jedis;
    private SerializableUtil serializableUtil = new SerializableUtil();
    private volatile boolean shutdown = false;

    public Consumer(Jedis jedis) {
        this.jedis = jedis;
    }


    @Override
    public void run() {
        byte[] bytes = jedis.rpop(PREPARE_SEND_MESSAGE_KEY.getBytes());
        while (bytes != null || !shutdown){
            if (bytes == null){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bytes = jedis.rpop(PREPARE_SEND_MESSAGE_KEY.getBytes());
                continue;
            }
            Message message = (Message) serializableUtil.decode(bytes);
            Channel channel = MqttHandler.channelGroup.find(message.getChannelId());
            if (channel == null){
                bytes = jedis.rpop(PREPARE_SEND_MESSAGE_KEY.getBytes());
                continue;
            }
            Jedis newJedis = RedisPoolUtil.getConn();
            SendMessageService sendMessageService = new SendMessageService(channel, message, newJedis);
            threadPoolExecutor.execute(sendMessageService);

            bytes = jedis.rpop(PREPARE_SEND_MESSAGE_KEY.getBytes());
        }
        jedis.close();
    }

    public void shutdonwRequest(){
        shutdown = true;
    }
}
