package cn.edu.ncu.common;

import cn.edu.ncu.domain.ClientInfo;
import cn.edu.ncu.domain.Message;
import cn.edu.ncu.domain.ServerSessionStatus;
import cn.edu.ncu.domain.TopicSub;
import io.netty.channel.ChannelId;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Singleton {
    private volatile static Properties serverConf;

    public static void loadServerConf() throws IOException {
        loadServerConf("/server.properties");
    }

    public static void loadServerConf(String s) throws IOException {
        if (serverConf == null){
            Properties props = new Properties();
            props.load(Singleton.class.getResourceAsStream(s));
            serverConf = props;
        }
    }

    public static Properties getServerConf()  {
        if (serverConf == null){
            try {
                loadServerConf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serverConf;
    }

////    客户端ID与会话状态的map
//    public static Map<String, ServerSessionStatus> sessionStatusMap = new Hashtable<String, ServerSessionStatus>();
////    channelId与clientId的map
//    public static Map<ChannelId, String> channelClientMap = new Hashtable<ChannelId, String>();
////    topic与topic信息map
//    public static Map<String, TopicSub> topicSubMap = new Hashtable<String, TopicSub>();
////
//    public static Map<String, ClientInfo> clientInfoMap = new Hashtable<String, ClientInfo>();
////    准备发送的信息
    public static BlockingQueue<Message> prepareSendMessage = new LinkedBlockingQueue<Message>();

    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(9, 17,
            4500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
}
