package com.geekbrains.donni.storage.cloud;

import com.geekbrains.donni.storage.cloud.controllers.AuthController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.geekbrains.donni.storage.cloud/auth.fxml"));
        Parent auth = loader.load();

        AuthController authController = loader.getController();
        authController.setStage(primaryStage);

        primaryStage.setTitle("Облачное хранилище by Donni || Авторизация");
        primaryStage.setScene(new Scene(auth));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
