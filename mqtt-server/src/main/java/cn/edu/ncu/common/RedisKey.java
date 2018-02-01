package cn.edu.ncu.common;

public class RedisKey {
//    clientId_client_status
    public final static String CLIENT_STATUS_KEY_MODEL = "%s_client_status";
//    channelId_channel_client
    public final static String CHANNEL_CLIENT_KEY_MODEL = "%s_channel_client";
//    clientId_client
    public final static String CLIENT_KEY_MODEL = "%s_client";
//    topicName_topic
    public final static String TOPIC_KEY_MODEL = "%s_topic";
    
    public final static String PREPARE_SEND_MESSAGE_KEY = "send_message";
//    messageId_message
    public final static String UNACK_MESSAGE_KEY_MODEL = "%s_message";
}
