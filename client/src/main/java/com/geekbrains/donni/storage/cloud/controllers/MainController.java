package com.geekbrains.donni.storage.cloud.controllers;

import com.geekbrains.donni.storage.cloud.network.Network;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private final String CLIENT_PATH = "D:/Games/World_of_Tanks_RU/screenshots";

    @FXML
    Button download;
    @FXML
    Button upload;
    @FXML
    Button deleteFile;
    @FXML
    ListView<String> listViewServer;
    @FXML
    ListView<String> listViewClient;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listViewClient = new ListView<>();
        listViewServer = new ListView<>();
        updateListViewClient();
//        updateListViewServer();
    }

    public void downloadFile(ActionEvent actionEvent) {
        String filename = listViewServer.getSelectionModel().toString();
        network.download(filename);
    }

    public void uploadFile(ActionEvent actionEvent) {
        String filename = listViewClient.getSelectionModel().toString();
        network.upload(filename);
    }

    public void updateListViewServer() {
        network.getInfoFromServerAboutStorage();
    }

    public void updateListViewClient() {
        File dir = new File(CLIENT_PATH);
        listViewClient.setItems(FXCollections.observableArrayList(dir.list()));
    }
}
