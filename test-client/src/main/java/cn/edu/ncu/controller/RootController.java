package cn.edu.ncu.controller;

import cn.edu.ncu.MainApp;
import cn.edu.ncu.model.*;
import cn.edu.ncu.utils.DialogUtil;
import cn.edu.ncu.utils.MqttUtil;
import com.alibaba.fastjson.JSON;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class RootController {

    public enum Tool{
        PAINT,
        ERASER
    }

    private final double MENU_WIDTH = 127;

    private final double LINE_WIDTH_MIN = 1;
    private final double LINE_WIDTH_MAX = 10;
    private final double LINE_WIDTH = 3;

    private final double ERASER_MAX = 100;
    private final double ERASER_MIN = 30;
    private final double ERASER_VALUE = 50;

    private GraphicsContext gc;
    private boolean isPressed = false;

    private double oldX;
    private double oldY;
    private double eraserSize = ERASER_VALUE;
    private double lineWidth = LINE_WIDTH;

    private MainApp mainApp;
    private MqttUtil mqttUtil;
    private Tool tool = Tool.PAINT;

    private SettingMessage setting;
    private SettingMessage mySetting;
    private boolean isMysetting = true;

    private LineMessages lineMessages;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private HBox hBox;
    @FXML
    private VBox vBox;
    @FXML
    private Pane pane;
    @FXML
    private Canvas canvas;
    @FXML
    private Button brushButton;
    @FXML
    private Button eraserButton;
    @FXML
    private Button clearAllButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider slider;
    @FXML
    private Slider eraserSlider;

    @FXML
    private void initialize(){
        setUp();
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(lineWidth);

        brushButton.setPrefWidth(vBox.getPrefWidth());
        eraserButton.setPrefWidth(vBox.getPrefWidth());
        clearAllButton.setPrefWidth(vBox.getPrefWidth());
        colorPicker.setPrefWidth(vBox.getPrefWidth());
        colorPicker.setValue((Color) gc.getStroke());
        slider.setPrefWidth(vBox.getPrefWidth());
        slider.setMin(LINE_WIDTH_MIN);
        slider.setMax(LINE_WIDTH_MAX);
        slider.setValue(LINE_WIDTH);
        eraserSlider.setPrefWidth(vBox.getPrefWidth());
        eraserSlider.setMax(ERASER_MAX);
        eraserSlider.setMin(ERASER_MIN);
        eraserSlider.setValue(ERASER_VALUE);

        updateMySetting();

        addCanvasHandler();
        addSliderHandler();
    }


    private void addSliderHandler() {
        slider.valueProperty().addListener(observable -> {
            lineWidth = slider.getValue();
            gc.setLineWidth(lineWidth);
            updateMySetting();
            if (mqttUtil != null && mqttUtil.isConnect()){
                MessagePackager messagePackager = new MessagePackager(SettingMessage.class,
                        JSON.toJSONString(mySetting).getBytes());
                mqttUtil.sendMessage(messagePackager);
            }
        });
        eraserSlider.valueProperty().addListener(observable -> {
            eraserSize = eraserSlider.getValue();
            updateMySetting();
            if (mqttUtil != null && mqttUtil.isConnect()){
                MessagePackager messagePackager = new MessagePackager(SettingMessage.class,
                        JSON.toJSONString(mySetting).getBytes());
                mqttUtil.sendMessage(messagePackager);
            }
        });
    }

    private void addCanvasHandler() {
        canvas.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && isMysetting){
                isPressed = true;
                lineMessages = new LineMessages();
                switch (tool){
                    case PAINT:
                        oldX = event.getX();
                        oldY = event.getY();
                        break;
                    case ERASER:
                        break;
                    default:
                        break;
                }
            }
        });
        canvas.setOnMouseDragged(event -> {
            if (isPressed && event.getButton() == MouseButton.PRIMARY && isMysetting){
                double newX = event.getX();
                double newY = event.getY();
                switch (tool){
                    case PAINT:
                        gc.strokeLine(oldX, oldY, newX, newY);
                        checkSize();
                        lineMessages.add(oldX, oldY, newX, newY);
                        oldX = newX;
                        oldY = newY;
                        break;
                    case ERASER:
                        gc.clearRect(newX, newY, eraserSize, eraserSize);
                        checkSize();
                        lineMessages.add(oldX, oldY, newX, newY);
                        break;
                    default:break;
                }
            }
        });
        canvas.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && isMysetting){
                isPressed = false;
                if (mqttUtil != null && mqttUtil.isConnect()){
                    MessagePackager messagePackager = new MessagePackager(LineMessages.class,
                            JSON.toJSONString(lineMessages).getBytes());
                    mqttUtil.sendMessage(messagePackager);
                }
            }
        });
    }

    private void checkSize() {
        if (lineMessages.getLineMessages().size() >= 30){
            if (mqttUtil != null && mqttUtil.isConnect()){
                MessagePackager messagePackager = new MessagePackager(LineMessages.class,
                        JSON.toJSONString(lineMessages).getBytes());
                mqttUtil.sendMessage(messagePackager);
            }
            lineMessages = new LineMessages();
        }
    }

    private void setUp(){
        vBox.setPrefWidth(MENU_WIDTH);
        pane.setPrefWidth(hBox.getPrefWidth()-vBox.getPrefWidth());
        pane.setPrefHeight(hBox.getPrefHeight());
        canvas.setHeight(pane.getPrefHeight());
        canvas.setWidth(pane.getPrefWidth());
    }

    public void loadImage(File file){
        WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            DialogUtil.show(Alert.AlertType.INFORMATION, "保存成功", "保存成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadImage(String path){
        File file = new File(path);
        loadImage(file);
    }

    @FXML
    private void save(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
        loadImage(file);
    }

    @FXML
    private void selectColor(){
        Color color = colorPicker.getValue();
        gc.setStroke(color);
        updateMySetting();
        if (mqttUtil != null && mqttUtil.isConnect()){
            MessagePackager messagePackager = new MessagePackager(SettingMessage.class,
                    JSON.toJSONString(mySetting).getBytes());
            mqttUtil.sendMessage(messagePackager);
        }
    }

    private void updateMySetting() {
        Color color = (Color) gc.getStroke();
        mySetting = new SettingMessage(color.getBlue(),
                color.getGreen(), color.getRed(), color.getOpacity(),
                lineWidth, eraserSize, tool);
    }

    @FXML
    private void paint(){
        tool = Tool.PAINT;
        updateMySetting();
        if (mqttUtil != null && mqttUtil.isConnect()){
            MessagePackager messagePackager = new MessagePackager(SettingMessage.class,
                    JSON.toJSONString(mySetting).getBytes());
            mqttUtil.sendMessage(messagePackager);
        }
    }

    @FXML
    private void eraser(){
        tool = Tool.ERASER;
        updateMySetting();
        if (mqttUtil != null && mqttUtil.isConnect()){
            MessagePackager messagePackager = new MessagePackager(SettingMessage.class,
                    JSON.toJSONString(mySetting).getBytes());
            mqttUtil.sendMessage(messagePackager);
        }
    }

    @FXML
    private void clearAll(){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (mqttUtil != null && mqttUtil.isConnect()){
            ClearAllMessage clearAllMessage = new ClearAllMessage();
            MessagePackager messagePackager = new MessagePackager(ClearAllMessage.class,
                JSON.toJSONString(clearAllMessage).getBytes());
            mqttUtil.sendMessage(messagePackager);
        }
    }

    @FXML
    private void showConnect(){
        if (mainApp.showConnect()){
            System.out.println("success");
            mqttUtil = mainApp.getMqttUtil();
            if (mqttUtil != null && mqttUtil.isConnect()){
                MessagePackager messagePackager = new MessagePackager(SettingMessage.class,
                        JSON.toJSONString(mySetting).getBytes());
                mqttUtil.sendMessage(messagePackager);
            }
        }
    }

    @FXML
    private void disconnect(){
        if (mqttUtil != null){
            mqttUtil.disconnect();
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        mainApp.getPrimaryStage().maximizedProperty().addListener(observable -> {
            anchorPane.setPrefHeight(mainApp.getPrimaryStage().getHeight());
            anchorPane.setPrefWidth(mainApp.getPrimaryStage().getWidth());
            System.out.println(anchorPane.getPrefHeight()+"x"+anchorPane.getPrefWidth());
        });
    }

    public void setSetting(SettingMessage setting){
        this.setting = setting;
    }

    public void doData(LineMessages lineMessages){
        isMysetting = false;
        changeSetting();
        for (LineMessage lineMessage: lineMessages.getLineMessages()){
            switch (tool){
                case PAINT:
                    gc.strokeLine(lineMessage.getOldX(), lineMessage.getOldY(), lineMessage.getNewX(), lineMessage.getNewY());
                    break;
                case ERASER:
                    gc.clearRect(lineMessage.getNewX(), lineMessage.getNewY(), eraserSize, eraserSize);
                    break;
                default:break;
            }
        }
        resetSetting();
        isMysetting = true;
    }

    public void clear(){
        clearAll();
    }

    private void changeSetting(){
        Color color = (Color) gc.getStroke();
        mySetting = new SettingMessage(color.getBlue(),
                color.getGreen(), color.getRed(), color.getOpacity(),
                lineWidth, eraserSize, tool);
        gc.setLineWidth(setting.getLineWidth());
        gc.setStroke(new Color(setting.getStorkRed(), setting.getStorkGreen(),
                setting.getStorkBlue(), setting.getStorkOpacity()));
        tool = setting.getTool();
        eraserSize = setting.getEraserSize();
    }

    private void resetSetting(){
        gc.setLineWidth(mySetting.getLineWidth());
        gc.setStroke(new Color(mySetting.getStorkRed(), mySetting.getStorkGreen(),
                mySetting.getStorkBlue(), mySetting.getStorkOpacity()));
        tool = mySetting.getTool();
        eraserSize = mySetting.getEraserSize();
    }
}
