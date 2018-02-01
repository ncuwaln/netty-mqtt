package cn.edu.ncu;

import cn.edu.ncu.model.LineMessage;
import cn.edu.ncu.model.MessagePackager;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

public class MessagePackagerTests {
    @Test
    public void test(){
        LineMessage lineMessage = new LineMessage(0, 0, 4, 4);
        MessagePackager messagePackager = new MessagePackager(LineMessage.class, JSON.toJSONString(lineMessage).getBytes());
        System.out.println(messagePackager.getMessageType().getSimpleName());
        LineMessage lineMessage1 = messagePackager.getContentObject();
        assert lineMessage.getNewX() == lineMessage1.getNewX();
    }
}
