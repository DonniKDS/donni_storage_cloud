package com.geekbrains.donni.storage.cloud.handlers;

import com.geekbrains.donni.storage.cloud.SignalBytes;
import com.geekbrains.donni.storage.cloud.option.ServerOption;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, FILE_NAME, FILE_NAME_LENGTH, FILE_LENGTH, FILE, DIRECTORY_INFO
    }

    private State currentState = State.IDLE;
    private String fileName;
    private int fileNameLength;
    private long fileLength;
    private long receivedFileLength;
    private FileInputStream fis;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == SignalBytes.UPLOAD_FILE) {
                    currentState = State.FILE_NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                    receiveFile(ctx, buf);
                } else if (readed == SignalBytes.DOWNLOAD_FILE) {
                    currentState = State.FILE_NAME_LENGTH;
                    System.out.println("STATE: Start file sending");
                    sendFile(ctx, buf);
                } else if (readed == SignalBytes.GET_INFO_ABOUT_DIRECTORY) {
                    currentState = State.DIRECTORY_INFO;
                    System.out.println("STATE: Start send directory information");
                    sendDirectoryInformation(ctx, buf);
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
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
                System.out.println("STATE: Get filename length");
                fileNameLength = buf.readInt();
                currentState = State.FILE_NAME;
            }
        }

        if (currentState == State.FILE_NAME) {
            if (buf.readableBytes() >= fileNameLength) {
                byte[] bytesFileName = new byte[fileNameLength];
                buf.readBytes(bytesFileName);
                fileName = new String(bytesFileName, StandardCharsets.UTF_8);
                System.out.println("STATE: Filename received - " + fileName);
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            System.out.println("STATE: Send file length");
            fileLength = Files.size(Paths.get(fileName));
            buf.writeFloat(fileLength);
            ctx.writeAndFlush(buf);
            currentState = State.FILE;
        }

        if (currentState == State.FILE) {
            System.out.println("STATE: Start sending file");
            buf.writeBytes(Files.readAllBytes(Paths.get(fileName)));
            ctx.writeAndFlush(buf);
            System.out.println("STATE: File sent");
            currentState = State.IDLE;
        }
    }

    private void receiveFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        if (currentState == State.FILE_NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println("STATE: Get filename length");
                fileNameLength = buf.readInt();
                currentState = State.FILE_NAME;
            }
        }

        if (currentState == State.FILE_NAME) {
            if (buf.readableBytes() >= fileNameLength) {
                byte[] fileName = new byte[fileNameLength];
                buf.readBytes(fileName);
                System.out.println("STATE: Filename received - " + new String(fileName, StandardCharsets.UTF_8));
                out = new BufferedOutputStream(new FileOutputStream(new String(fileName, StandardCharsets.UTF_8)));
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println("STATE: File length received - " + fileLength);
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    currentState = State.IDLE;
                    System.out.println("File received");
                    out.close();
                    break;
                }
            }
        }
    }

    private void sendDirectoryInformation(ChannelHandlerContext ctx, ByteBuf buf) {
        if (currentState == State.DIRECTORY_INFO) {
            File dir = new File(ServerOption.DIRECTORY);
            List<String> list = Arrays.asList(Objects.requireNonNull(dir.list()));
            list.toArray();
//            buf.writeBytes();
            currentState = State.IDLE;
        }
    }
}
