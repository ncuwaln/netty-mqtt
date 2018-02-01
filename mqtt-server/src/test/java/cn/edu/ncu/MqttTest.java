package cn.edu.ncu;


import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;


public class MqttTest {

    String topic        = "MQTT Examples";
    byte[] bs = topic.getBytes();

    String content      = "Message from MqttPublishSample";
    int qos             = 0;
    String broker       = "tcp://127.0.0.1:12345";
    String clientId     = "guest";
    MemoryPersistence persistence = new MemoryPersistence();


    @Test
    public void test(){
        MqttThread mqttRunnable = new MqttThread();
        Thread mqttThread = new Thread(mqttRunnable);
        mqttThread.start();
        try {
            final MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("l=====");
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    System.out.println("a=====");
                    System.out.println("payload: "+new String(mqttMessage.getPayload()));
//                    sampleClient.messageArrivedComplete(mqttMessage.getId(), 1);
                    new MqttPubAck(mqttMessage.getId());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    try {
                        System.out.println("payload: "+new String(iMqttDeliveryToken.getMessage().getPayload()));
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    System.out.println("c=====");
                }
            });
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
//            sampleClient.subscribe(topic, qos);
            MqttMessage message = new MqttMessage(content.getBytes());
            sampleClient.publish(topic, message);
//            sampleClient.unsubscribe(topic);
//            sampleClient.disconnect();
            mqttThread.join();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class MqttThread implements Runnable{
        String topic        = "MQTT Examples";
        byte[] bs = topic.getBytes();

        String content      = "Message from MqttPublishSample";
        int qos             = 1;
        String broker       = "tcp://127.0.0.1:12345";
        String clientId     = "receiver";
        MemoryPersistence persistence = new MemoryPersistence();

        @Override
        public void run() {
                try {
                    final MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                    sampleClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable throwable) {
                            System.out.println("l=====");
                        }

                        @Override
                        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                            System.out.println("receiver=====");
                            System.out.println("payload: "+new String(mqttMessage.getPayload()));
                            sampleClient.messageArrivedComplete(mqttMessage.getId(), 1);
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                            try {
                                System.out.println("payload: "+new String(iMqttDeliveryToken.getMessage().getPayload()));
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                            System.out.println("c=====");
                        }
                    });
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                    sampleClient.connect(connOpts);
                    sampleClient.subscribe(topic, qos);
                    Thread.sleep(2000);
                    sampleClient.unsubscribe(topic);
                    System.out.println("unsubscribe");
                    sampleClient.disconnect();
                    System.out.println("disconnect");
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
}
