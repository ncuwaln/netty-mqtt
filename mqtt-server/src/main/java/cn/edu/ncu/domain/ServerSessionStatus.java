package cn.edu.ncu.domain;

import io.netty.handler.codec.mqtt.MqttTopicSubscription;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerSessionStatus implements Serializable{
    private static final long serialVersionUID = -1228789421784645244L;
    private List<Sub> subscriptions = new LinkedList<Sub>();
    private List<Message> unAckMessages = new LinkedList<Message>();
    private List<Message> unSendMessage = new LinkedList<Message>();


    public List<Sub> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Sub> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<Message> getUnAckMessages() {
        return unAckMessages;
    }

    public void setUnAckMessages(List<Message> unAckMessages) {
        this.unAckMessages = unAckMessages;
    }

    public List<Message> getUnSendMessage() {
        return unSendMessage;
    }

    public void setUnSendMessage(List<Message> unSendMessage) {
        this.unSendMessage = unSendMessage;
    }

    public void unSubscribe(String[] topicName){
        Set<String> topicSet = new HashSet<String>(Arrays.asList(topicName));
        Iterator<Sub> iterator = subscriptions.iterator();
        while (iterator.hasNext()){
            Sub subscription = iterator.next();
            if (topicSet.contains(subscription.getTopicName())){
                iterator.remove();
            }
        }

    }
}
