package cn.edu.ncu.utils;

import cn.edu.ncu.controller.RootController;
import cn.edu.ncu.model.SettingMessage;

public class SingletonUtil {
    private static RootController rootController;

    public static void setRootController(RootController r){
        if (rootController == null){
            rootController = r;
        }
    }

    public static RootController getRootController() {
        if (rootController == null){
            throw new NullPointerException();
        }
        return rootController;
    }
}
