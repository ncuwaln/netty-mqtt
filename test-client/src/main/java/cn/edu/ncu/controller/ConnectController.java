package cn.edu.ncu.controller;

import cn.edu.ncu.utils.DialogUtil;
import cn.edu.ncu.utils.MqttUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;

public class ConnectController {

    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField topicField;
    @FXML
    private ChoiceBox<Integer> qosBox;

    private MqttUtil mqttUtil;
    private Stage dialogStage;
    private boolean isSuccess = false;

    @FXML
    private void initialize(){
        qosBox.setValue(0);
        qosBox.setItems(FXCollections.observableArrayList(0, 1, 2));
    }

    @FXML
    private void handleOK(){
        if (inputValid()){
            String host = hostField.getText();
            String port = portField.getText();
            String url = String.format("tcp://%s:%s", host, port);
            System.out.println(url);
            try {
                mqttUtil = new MqttUtil(url);
                mqttUtil.init(topicField.getText(), qosBox.getValue());
                isSuccess = true;
            } catch (MqttException e) {
                e.printStackTrace();
                DialogUtil.show(Alert.AlertType.ERROR,
                        "Mqtt server连接失败", "Mqtt server连接失败");
            }finally {
                dialogStage.close();
            }
        }
    }

    private boolean inputValid() {
        if (hasText(hostField.getText()) && hasText(portField.getText()) && hasText(topicField.getText())){
            return true;
        }else {
            DialogUtil.show(Alert.AlertType.WARNING, "不能为空", "所有字段都不能为空");
            return false;
        }
    }

    private boolean hasText(String text) {
        return text != null && !text.isEmpty();
    }

    @FXML
    private void handleCancel(){
        dialogStage.close();
    }

    public boolean isSuccess(){
        return isSuccess;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public MqttUtil getMqttUtil() {
        return mqttUtil;
    }
}
