package cn.edu.ncu.utils;

import javafx.scene.control.Alert;

public class DialogUtil {

    public static void show(Alert.AlertType type, String title, String content){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
