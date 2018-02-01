package cn.edu.ncu.domain;

import io.netty.channel.ChannelId;

import java.io.Serializable;

public class Sub implements Serializable{

    private static final long serialVersionUID = -7177087535211800080L;

    private ChannelId channelId;
    private int qos;
    private String topicName;

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sub sub = (Sub) o;

        if (qos != sub.qos) return false;
        return channelId != null ? channelId.asLongText().equals(sub.channelId.asLongText()) : sub.channelId == null;
    }

    @Override
    public int hashCode() {
        int result = channelId != null ? channelId.hashCode() : 0;
        result = 31 * result;
        return result;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
