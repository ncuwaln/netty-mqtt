package cn.edu.ncu.model;

import com.alibaba.fastjson.JSON;

public class MessagePackager {

    private Class messageType;
    private byte[] messageContent;

    public MessagePackager(Class messageType, byte[] messageContent) {
        this.messageType = messageType;
        this.messageContent = messageContent;
    }


    public Class getMessageType() {
        return messageType;
    }

    public void setMessageType(Class messageType) {
        this.messageType = messageType;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    public <T> T getContentObject(){
        return JSON.parseObject(messageContent, messageType);
    }
}
