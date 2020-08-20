package com.geekbrains.donni.storage.cloud.handlers;

import com.geekbrains.donni.storage.cloud.ServerApp;
import com.geekbrains.donni.storage.cloud.SignalBytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE, LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD, TRY_AUTH
    }

    private State currentState = State.IDLE;
    private boolean authOK = false;
    private int loginLength;
    private String login;
    private int passwordLength;
    private String password;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (!authOK) {

            while (buf.readableBytes() > 0) {

                if (currentState == State.IDLE) {
                    byte readed = buf.readByte();
                    if (readed == SignalBytes.AUTH) {
                        System.out.println("STATE: Пользователь пытается авторизоваться");
                        currentState = State.LOGIN_LENGTH;
                    }
                }

                if (currentState == State.LOGIN_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        loginLength = buf.readInt();
                        System.out.println("STATE: Получили длину логина");
                        currentState = State.LOGIN;
                    }
                }

                if (currentState == State.LOGIN) {
                    if (buf.readableBytes() >= loginLength) {
                        byte[] byteLogin = new byte[loginLength];
                        buf.readBytes(byteLogin);
                        login = new String(byteLogin, StandardCharsets.UTF_8);
                        System.out.println("STATE: Получили логин - " + login);
                        currentState = State.PASSWORD_LENGTH;
                    }
                }

                if (currentState == State.PASSWORD_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        System.out.println("STATE: Получили длину пароля");
                        passwordLength = buf.readInt();
                        currentState = State.PASSWORD;
                    }
                }

                if (currentState == State.PASSWORD) {
                    if (buf.readableBytes() >= passwordLength) {
                        byte[] bytePass = new byte[passwordLength];
                        buf.readBytes(bytePass);
                        password = new String(bytePass, StandardCharsets.UTF_8);
                        System.out.println("STATE: Получили пароль");
                        currentState = State.TRY_AUTH;
                    }
                }

                if (currentState == State.TRY_AUTH) {
                    System.out.println("STATE: Сравниваем имя пользователя и пароль с базой данных");
                    authOK = ServerApp.getAuthService().getUsernameByLoginAndPassword(login, password);
                    if (authOK) {
                        buf.writeByte(SignalBytes.AUTH_OK);
                        ctx.writeAndFlush(buf);
                        MainHandler.username = login;
                        System.out.println("Auth - OK");
                        break;
                    } else {
                        buf.writeByte(SignalBytes.AUTH_NOT_OK);
                        ctx.writeAndFlush(buf);
                        System.out.println("Auth - NOT OK");
                    }
                    currentState = State.IDLE;
                }
            }
        }
        ctx.pipeline().remove(this);
    }
}
