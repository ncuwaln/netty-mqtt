package cn.edu.ncu.utils;

import cn.edu.ncu.controller.RootController;
import cn.edu.ncu.model.*;

public class MessageHandler {

    public void handleMessage(MessagePackager messagePackager){
        String messageTypeName = messagePackager.getMessageType().getName();
        if (messageTypeName.equals(LineMessages.class.getName())){
            LineMessages lineMessages = messagePackager.getContentObject();
            doLineMessages(lineMessages);
        }else if (messageTypeName.equals(SettingMessage.class.getName())){
            SettingMessage settingMessage = messagePackager.getContentObject();
            doSettingMessage(settingMessage);
        }else if (messageTypeName.equals(ClearAllMessage.class.getName())){
            ClearAllMessage clearAllMessage = messagePackager.getContentObject();
            doClearAll(clearAllMessage);
        }else {
            System.out.println("Unknown MessageType");
        }
    }

    private void doLineMessages(LineMessages lineMessages) {
        SingletonUtil.getRootController().doData(lineMessages);
    }

    private void doClearAll(ClearAllMessage clearAllMessage) {
        if (clearAllMessage.isClearAll()){
            SingletonUtil.getRootController().clear();
        }
    }

    private void doSettingMessage(SettingMessage settingMessage) {
        RootController rootController = SingletonUtil.getRootController();
        rootController.setSetting(settingMessage);
    }

}
