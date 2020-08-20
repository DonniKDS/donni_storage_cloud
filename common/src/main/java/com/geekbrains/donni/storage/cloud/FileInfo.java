package com.geekbrains.donni.storage.cloud;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileInfo {

    private SimpleStringProperty fullFileName;
    private SimpleStringProperty fileName;
    private SimpleStringProperty fileExtension;
    private SimpleLongProperty fileSize;


    public FileInfo(String fullFileName, long fileSize) {
        this.fullFileName = new SimpleStringProperty(fullFileName);
        this.fileSize = new SimpleLongProperty(fileSize);
        this.fileName = new SimpleStringProperty(Util.getFileName(fullFileName));
        this.fileExtension = new SimpleStringProperty(Util.getFileExtension(fullFileName));
    }

    public SimpleStringProperty getFullFileNameProperty() {
        return fullFileName;
    }

    public SimpleStringProperty getFileNameProperty() {
        return fileName;
    }

    public SimpleStringProperty getFileExtensionProperty() {
        return fileExtension;
    }

    public SimpleLongProperty getFileSizeProperty() {
        return fileSize;
    }

    public String fullFileName() {
        return fullFileName.get();
    }

    public String fileName() {
        return fileName.get();
    }

    public String fileExtension() {
        return fileExtension.get();
    }

    public long fileSize() {
        return fileSize.get();
    }

    public void setFullFileName(String fullFileName) {
        this.fullFileName.set(fullFileName);
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension.set(fileExtension);
    }

    public void setFileSize(long fileSize) {
        this.fileSize.set(fileSize);
    }
}
