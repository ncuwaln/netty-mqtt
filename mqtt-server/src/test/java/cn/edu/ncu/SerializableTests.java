package cn.edu.ncu;

import cn.edu.ncu.domain.Sub;
import io.netty.channel.DefaultChannelId;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class SerializableTests {

    private Sub sub;

    @Test
    public void setUp(){
        sub = new Sub();
        sub.setChannelId(DefaultChannelId.newInstance());
        sub.setQos(1);

        String channelId = sub.getChannelId().asLongText();
        int qos = sub.getQos();
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.writeObject(sub);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sub sub1 = null;
        try {
            sub1 = (Sub) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        assert sub1.getChannelId().asLongText().equals(channelId);
        assert sub1.getQos() == qos;
    }
}
