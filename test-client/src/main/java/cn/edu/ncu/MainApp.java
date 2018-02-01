package cn.edu.ncu;

import cn.edu.ncu.controller.ConnectController;
import cn.edu.ncu.controller.RootController;
import cn.edu.ncu.utils.MqttUtil;
import cn.edu.ncu.utils.SingletonUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ConnectController connectController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Online Canvas");
        this.primaryStage.setResizable(false);

        initialize();
    }

    private void initialize() {

        try {
//            init root
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cn/edu/ncu/view/Root.fxml"));
            rootLayout = loader.load();
            RootController rootController = loader.getController();
            rootController.setMainApp(this);

            SingletonUtil.setRootController(rootController);

//            Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public boolean showConnect(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/cn/edu/ncu/view/Connect.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Connect");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            connectController = loader.getController();
            connectController.setDialogStage(dialogStage);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return connectController.isSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MqttUtil getMqttUtil(){
        if (connectController == null){
            return null;
        }
        return connectController.getMqttUtil();
    }
}
