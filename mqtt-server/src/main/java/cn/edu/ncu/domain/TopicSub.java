package cn.edu.ncu.domain;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class TopicSub implements Serializable{
    private static final long serialVersionUID = 7919327722337655829L;
    private Set<Sub> subs = new LinkedHashSet<Sub>();
    private String topicName;

    public Set<Sub> getSubs() {
        return subs;
    }

    public void setSubs(Set<Sub> subs) {
        this.subs = subs;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void ubSubscribe(String channelId){
        Iterator<Sub> subIterator = subs.iterator();
        while (subIterator.hasNext()){
            Sub sub = subIterator.next();
            if (sub.getChannelId().equals(channelId)){
                subIterator.remove();
            }
        }
    }
}
