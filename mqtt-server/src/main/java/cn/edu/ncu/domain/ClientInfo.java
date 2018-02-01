package cn.edu.ncu.domain;

import io.netty.channel.ChannelId;

import java.io.Serializable;

public class ClientInfo implements Serializable{

    private static final long serialVersionUID = -2041778345553280593L;
    private String clientIdentifier;
    private ChannelId channelId;
    private int keepAlive;

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }
}
