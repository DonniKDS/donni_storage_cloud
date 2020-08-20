package com.geekbrains.donni.storage.cloud.handlers;

import com.geekbrains.donni.storage.cloud.SignalBytes;
import com.geekbrains.donni.storage.cloud.option.ServerOption;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class MainHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, FILE_NAME, FILE_NAME_LENGTH, FILE_LENGTH, FILE, DIRECTORY_INFO
    }

    protected static String username;
    private State currentState = State.IDLE;
    private int fileNameLength;
    private String fileName;
    private long fileLength;
    private long receivedFileLength;
    private FileInputStream fis;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte readed = buf.readByte();
        if (currentState == State.IDLE) {
            System.out.println("STATE: Получили команду");
            if (readed == SignalBytes.UPLOAD_FILE) {
                currentState = State.FILE_NAME_LENGTH;
                receivedFileLength = 0L;
                System.out.println("STATE: Начинаем сохранять файл на сервере");
                receiveFile(buf);
            } else if (readed == SignalBytes.DOWNLOAD_FILE) {
                currentState = State.FILE_NAME_LENGTH;
                System.out.println("STATE: Начинает отправку файла клиенту");
                sendFile(ctx, buf);
            } else if (readed == SignalBytes.GET_INFO_ABOUT_DIRECTORY) {
                currentState = State.DIRECTORY_INFO;
                System.out.println("STATE: Отправляем информацию о файлах на сервере");
                sendDirectoryInformation(ctx, buf);
            } else {
                System.out.println("ERROR: Неверный первый байт - " + readed);
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        if (currentState == State.FILE_NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println("STATE: Получили длину имени файла (fileNameLength)");
                fileNameLength = buf.readInt();
                currentState = State.FILE_NAME;
            }
        }

        if (currentState == State.FILE_NAME) {
            if (buf.readableBytes() >= fileNameLength) {
                byte[] bytesFileName = new byte[fileNameLength];
                buf.readBytes(bytesFileName);
                fileName = ServerOption.DIRECTORY + username + "/" + new String(bytesFileName, StandardCharsets.UTF_8);
                System.out.println("STATE: Имя файла получено - " + fileName);
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            System.out.println("STATE: Отправляем длину файла");
            fileLength = Files.size(Paths.get(fileName));
            buf.writeLong(fileLength);
            ctx.write(buf);
            currentState = State.FILE;
        }

        if (currentState == State.FILE) {
            System.out.println("STATE: Отправляем файл");
            buf.writeBytes(Files.readAllBytes(Paths.get(fileName)));
            ctx.writeAndFlush(buf);
            System.out.println("STATE: Файл отправлен");
            currentState = State.IDLE;
        }
    }

    private void receiveFile(ByteBuf buf) throws IOException {
        if (currentState == State.FILE_NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println("STATE: Получаем длину имени файла");
                fileNameLength = buf.readInt();
                currentState = State.FILE_NAME;
            }
        }

        if (currentState == State.FILE_NAME) {
            if (buf.readableBytes() >= fileNameLength) {
                byte[] fileName = new byte[fileNameLength];
                buf.readBytes(fileName, 0, fileNameLength);
                String file = new String(fileName, StandardCharsets.UTF_8);
                System.out.println("STATE: Имя файла получено - " + file);
                Files.createFile(Paths.get(ServerOption.DIRECTORY + username + "/" + file));
                out = new BufferedOutputStream(new FileOutputStream(ServerOption.DIRECTORY + username + "/" + file));
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println("STATE: Длина файла получена - " + fileLength);
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    currentState = State.IDLE;
                    System.out.println("Файл сохранен на сервере");
                    out.close();
                    break;
                }
            }
        }
    }

    private void sendDirectoryInformation(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        if (currentState == State.DIRECTORY_INFO) {
            File dir = new File(ServerOption.ABSOLUTE_PATH + "\\" + username);
            String[] filesList = dir.list();
            if (filesList.length != 0) {
                for (String sFile : filesList) {
                    long fileSize = Files.size(Paths.get(ServerOption.ABSOLUTE_PATH + "\\" + username + "\\" + sFile));
                    buf.writeByte(SignalBytes.START_SENDING_INFO_ABOUT_DIRECTORY);
                    buf.writeInt(sFile.getBytes().length);
                    buf.writeBytes(sFile.getBytes());
                    buf.writeLong(fileSize);
                }
            }
            buf.writeByte(SignalBytes.STOP_SENDING_INFO_ABOUT_DIRECTORY);
            ctx.writeAndFlush(buf);
            System.out.println("STATE: Отправили информацию");
            currentState = State.IDLE;
        }
    }
}
