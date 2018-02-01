package cn.edu.ncu.domain;


import io.netty.channel.ChannelId;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;


import java.io.Serializable;

public class Message implements Serializable{
    private static final long serialVersionUID = -679749366398538057L;
    private String topicName;
    private ChannelId channelId;
    private MqttPublishMessage sendMessage;
    private MqttMessageType status;
    private int packageId;
    private byte[] content;
    private int qoS;
    private  boolean isDup;
    private  boolean isRetain;
    private  int remainingLength;

    public boolean isDup() {
        return isDup;
    }

    public void setDup(boolean dup) {
        isDup = dup;
    }

    public boolean isRetain() {
        return isRetain;
    }

    public void setRetain(boolean retain) {
        isRetain = retain;
    }

    public int getRemainingLength() {
        return remainingLength;
    }

    public void setRemainingLength(int remainingLength) {
        this.remainingLength = remainingLength;
    }

    public int getQoS() {
        return qoS;
    }

    public void setQoS(int qoS) {
        this.qoS = qoS;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public MqttMessageType getStatus() {
        return status;
    }

    public void setStatus(MqttMessageType status) {
        this.status = status;
    }
}
