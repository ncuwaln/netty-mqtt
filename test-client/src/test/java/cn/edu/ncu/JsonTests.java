package cn.edu.ncu;

import cn.edu.ncu.controller.RootController;
import cn.edu.ncu.model.LineMessage;
import cn.edu.ncu.model.LineMessages;
import cn.edu.ncu.model.SettingMessage;
import com.alibaba.fastjson.JSON;
import javafx.scene.paint.Color;
import org.junit.Test;

public class JsonTests {

    @Test
    public void test(){

        Color color = Color.CORAL;
        SettingMessage settingMessage = new SettingMessage(color.getBlue(),
                color.getGreen(), color.getRed(), color.getOpacity(),
                3, 4, RootController.Tool.PAINT);
        String str1 = JSON.toJSONString(settingMessage);
        SettingMessage settingMessage1 = JSON.parseObject(str1, SettingMessage.class);
        assert settingMessage.storkColor().equals(settingMessage1.storkColor());
    }

    @Test
    public void testLineMessages(){
        LineMessages lineMessages = new LineMessages();
        lineMessages.add(0,0,1,2);
        lineMessages.add(0,2,3,4);
        String str = JSON.toJSONString(lineMessages);
        LineMessages lineMessages1 = JSON.parseObject(str, LineMessages.class);
        assert lineMessages.getLineMessages().size() == lineMessages1.getLineMessages().size();
    }
}
