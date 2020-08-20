package com.geekbrains.donni.storage.cloud.network;

import com.geekbrains.donni.storage.cloud.FileInfo;
import com.geekbrains.donni.storage.cloud.SignalBytes;
import com.geekbrains.donni.storage.cloud.Util;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Network {

    private final String HOST = "localhost";
    private final int PORT = 8189;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authOk;

    public Network() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAuthOk() {
        return authOk;
    }

    public void tryAuth(String login, String password) {
        try {
            out.writeByte(SignalBytes.AUTH);
            out.writeInt(login.getBytes(StandardCharsets.UTF_8).length);
            out.writeBytes(login);
            out.writeInt(password.getBytes(StandardCharsets.UTF_8).length);
            out.writeBytes(password);
            byte auth = in.readByte();
            if (auth == SignalBytes.AUTH_OK) {
                authOk = true;
            } else if (auth == SignalBytes.AUTH_NOT_OK) {
                authOk = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void uploadFile(String filename, String filePath, long fileSize) {
        try {
            out.writeByte(SignalBytes.UPLOAD_FILE);
            System.out.println("Отправили команду");
            out.writeInt(filename.getBytes(StandardCharsets.UTF_8).length);
            System.out.println("Отправили длину имени файла");
            out.writeBytes(filename);
            System.out.println("Отправили имя файла");
            out.writeLong(fileSize);
            System.out.println("Отправили длину файла");
            FileInputStream fis = new FileInputStream(filePath + "/" + filename);
            byte[] buffer = new byte[10 * 1024 * 1024];
            int readBytes;
            while (fis.available() > 0) {
                readBytes = fis.read(buffer);
                out.write(buffer, 0, readBytes);
            }
            System.out.println("Отправили файл");
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String filename, String pathForFile) {
        try {
            out.writeByte(SignalBytes.DOWNLOAD_FILE);
            out.writeInt(filename.getBytes(StandardCharsets.UTF_8).length);
            out.writeBytes(filename);
            Path file = Paths.get( pathForFile + "/" + filename);
            int count = 0;
            while (Files.exists(file)) {
                count++;
                file = Paths.get(pathForFile + "/" + Util.getFileName(filename) + "(" + count + ")" + Util.getFileExtension(filename));
            }
            Files.createFile(file);
            try (FileOutputStream fos = new FileOutputStream(file.toFile(), true))
            {
                long fileLength = in.readLong();
                byte[] buffer = new byte[10 * 1024 * 1024];
                int readBytes;
                while (Files.size(file) != fileLength) {
                    readBytes = in.read(buffer);
                    fos.write(buffer, 0, readBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getInfoFromServerAboutStorage (ObservableList<FileInfo> listFiles) {
        try {
            out.writeByte(SignalBytes.GET_INFO_ABOUT_DIRECTORY);
            byte readBytes = in.readByte();
            while (readBytes != SignalBytes.STOP_SENDING_INFO_ABOUT_DIRECTORY) {
                int fileNameLength = in.readInt();
                byte[] bytesFileName = new byte[fileNameLength];
                in.read(bytesFileName);
                String fileName = new String(bytesFileName);
                long fileSize = in.readLong();
                listFiles.add(new FileInfo(fileName, fileSize));
                readBytes = in.readByte();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
