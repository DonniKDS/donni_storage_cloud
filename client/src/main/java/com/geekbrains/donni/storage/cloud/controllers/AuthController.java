package com.geekbrains.donni.storage.cloud.controllers;

import com.geekbrains.donni.storage.cloud.network.Network;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    private Network network;
    private Stage stage;
    private String login;
    private String password;

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network();
        loginButton.setOnAction(event -> {
            login = loginField.getText();
            password = passwordField.getText();
            network.tryAuth(login, password);
            if (network.isAuthOk()) {
                loginButton.getScene().getWindow().hide();
                createNewScene();
            }
        });
    }

    private void createNewScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.geekbrains.donni.storage.cloud/main.fxml"));
            Parent root = loader.load();;

            MainController mainController = loader.getController();
            mainController.setNetwork(network);

            stage.setScene(new Scene(root));
            stage.setTitle("Облачное хранилище by Donni");
            stage.setResizable(false);
            stage.show();

            mainController.updateListViewClient();
            mainController.updateListViewServer();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
