package com.geekbrains.donni.storage.cloud.controllers;

import com.geekbrains.donni.storage.cloud.FileInfo;
import com.geekbrains.donni.storage.cloud.network.Network;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.LongToDoubleFunction;

public class MainController implements Initializable {

    private final String CLIENT_PATH = "user/";
    private final String FULL_CLIETN_PATH = "D:\\Geekbrains\\donni_storage_cloud\\user";

    @FXML
    private Button downloadButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<FileInfo> tableViewServer;
    @FXML
    private TableView<FileInfo> tableViewClient;
    @FXML
    private TableColumn<FileInfo, String> nameTableColumnServer, extensionTableColumnServer, nameTableColumnClient, extensionTableColumnClient;
    @FXML
    private TableColumn<FileInfo, Long>  sizeTableColumnServer, sizeColumnTableClient;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void downloadFile(ActionEvent actionEvent) {
        String filename = tableViewServer.getFocusModel().getFocusedItem().fullFileName();
        network.downloadFile(filename, CLIENT_PATH);
        updateListViewClient();
    }

    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String filename = tableViewClient.getFocusModel().getFocusedItem().fullFileName();
        long fileSize = Files.size(Paths.get(CLIENT_PATH + filename));
        network.uploadFile(filename, CLIENT_PATH, fileSize);
        updateListViewServer();
    }

    public void deleteFile(ActionEvent actionEvent) {
        updateListViewServer();
        updateListViewClient();
    }

    public void updateListViewServer() {
        ObservableList<FileInfo> fileList = FXCollections.observableArrayList();
        network.getInfoFromServerAboutStorage(fileList);
        tableViewServer.getItems().clear();
        nameTableColumnServer.setCellFactory(TextFieldTableCell.forTableColumn());
        nameTableColumnServer.setCellValueFactory(cellData -> cellData.getValue().getFileNameProperty());
        extensionTableColumnServer.setCellFactory(TextFieldTableCell.forTableColumn());
        extensionTableColumnServer.setCellValueFactory(cellData -> cellData.getValue().getFileExtensionProperty());
        sizeTableColumnServer.setCellFactory(new Callback<TableColumn<FileInfo, Long>, TableCell<FileInfo, Long>>() {
            @Override
            public TableCell<FileInfo, Long> call(TableColumn<FileInfo, Long> param) {
                TableCell<FileInfo, Long> cell = new TableCell<FileInfo, Long>() {
                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setText("");
                            return;
                        }
                        float itemFloat = (float) item;
                        float textFloat = itemFloat/1000000;
                        BigDecimal text = new BigDecimal(Float.toString(textFloat));
                        text = text.setScale(2, RoundingMode.HALF_UP);
                        setText(text.toString());
                    }
                };
                return cell;
            }
        });
        sizeTableColumnServer.setCellValueFactory(cellData -> cellData.getValue().getFileSizeProperty().asObject());
        tableViewServer.setItems(fileList);
    }

    public void updateListViewClient() {
        ObservableList<FileInfo> fileList = FXCollections.observableArrayList();
        File dir = new File(FULL_CLIETN_PATH);
        String[] list = dir.list();
        if (list.length != 0) {
            for (String file : list) {
                try {
                    long fileSize = Files.size(Paths.get(FULL_CLIETN_PATH + "\\" + file));
                    fileList.add(new FileInfo(file, fileSize));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        tableViewClient.getItems().clear();
        nameTableColumnClient.setCellFactory(TextFieldTableCell.forTableColumn());
        nameTableColumnClient.setCellValueFactory(cellData -> cellData.getValue().getFileNameProperty());
        extensionTableColumnClient.setCellFactory(TextFieldTableCell.forTableColumn());
        extensionTableColumnClient.setCellValueFactory(cellData -> cellData.getValue().getFileExtensionProperty());
        sizeColumnTableClient.setCellFactory(new Callback<TableColumn<FileInfo, Long>, TableCell<FileInfo, Long>>() {
            @Override
            public TableCell<FileInfo, Long> call(TableColumn<FileInfo, Long> param) {
                TableCell<FileInfo, Long> cell = new TableCell<FileInfo, Long>() {
                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setText("");
                            return;
                        }
                        float itemFloat = (float) item;
                        float textFloat = itemFloat/1000000;
                        BigDecimal text = new BigDecimal(Float.toString(textFloat));
                        text = text.setScale(2, RoundingMode.HALF_UP);
                        setText(text.toString());
                    }
                };
                return cell;
            }
        });
        sizeColumnTableClient.setCellValueFactory(cellData -> cellData.getValue().getFileSizeProperty().asObject());
        tableViewClient.setItems(fileList);
    }
}
