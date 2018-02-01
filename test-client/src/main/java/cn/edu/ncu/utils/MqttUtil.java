package cn.edu.ncu.utils;

import cn.edu.ncu.exception.EmptyListException;
import cn.edu.ncu.model.MessagePackager;
import com.alibaba.fastjson.JSON;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MqttUtil {
    private String broker;
    private String clientId;
    private MqttClient client;
    private String topic;
    private int qos;
    private MemoryPersistence persistence = new MemoryPersistence();
    private MessageHandler messageHandler = new MessageHandler();

    private NoRepeatList<String> usedMessageIdList = new NoRepeatList<>();
    private NoRepeatList<String> ackMessageIdList = new NoRepeatList<>();
    private int maxMessageId = 0;

    public MqttUtil(String broker) throws MqttException {
        this(broker, UUID.randomUUID().toString());
    }

    public MqttUtil(String broker, String clientId) throws MqttException {
        this.broker = broker;
        this.clientId = clientId;
        client = new MqttClient(this.broker, this.clientId, persistence);
        client.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable throwable) {
            System.out.println("connection lost");
        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            if (!usedMessageIdList.contains(String.valueOf(mqttMessage.getId()))){
                MessagePackager messagePackager = JSON.parseObject(mqttMessage.getPayload(), MessagePackager.class);
                ackMessageIdList.add(String.valueOf(mqttMessage.getId()));
                messageHandler.handleMessage(messagePackager);
            }
            client.messageArrivedComplete(mqttMessage.getId(), 1);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            System.out.println("deliveryComplete");
            String messageId = String.valueOf(iMqttDeliveryToken.getMessageId());
            usedMessageIdList.remove(messageId);
            ackMessageIdList.add(messageId);
        }
    });
}

    public void sendMessage(MessagePackager messagePackager, int qos, boolean retained)  {
        byte[] payload = JSON.toJSONString(messagePackager).getBytes();
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        message.setId(getMessageId());
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void init(String topic, int qos) throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        client.connect(connOpts);
        client.subscribe(topic, qos);
        this.topic = topic;
        this.qos = qos;
    }

    public void sendMessage(MessagePackager messagePackager){
        sendMessage(messagePackager, qos, false);
    }

    private int getMessageId() {
        String messageIdStr = "";
        if (!ackMessageIdList.isEmpty()){
            try {
                messageIdStr = ackMessageIdList.pop();
            } catch (EmptyListException e) {
                e.printStackTrace();
            }
        }else{
            messageIdStr = String.valueOf(maxMessageId+1);
            usedMessageIdList.add(messageIdStr);
        }
        return Integer.valueOf(messageIdStr);
    }

    public boolean isConnect(){
        return client.isConnected();
    }

    public void disconnect(){
        if (client.isConnected()){
            try {
                client.unsubscribe(topic);
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
