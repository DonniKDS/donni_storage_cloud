package com.geekbrains.donni.storage.cloud;

import com.geekbrains.donni.storage.cloud.database.AuthService;
import com.geekbrains.donni.storage.cloud.database.BaseAuthService;
import com.geekbrains.donni.storage.cloud.handlers.AuthHandler;
import com.geekbrains.donni.storage.cloud.handlers.MainHandler;
import com.geekbrains.donni.storage.cloud.option.ServerOption;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerApp {

    private static final AuthService authService = new BaseAuthService();;

    public static AuthService getAuthService() {
        return authService;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        authService.start();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup);
            server.channel(NioServerSocketChannel.class);
            server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new AuthHandler(), new MainHandler());
                }
            });
            ChannelFuture future = server.bind(ServerOption.PORT).sync();
            System.out.println("Сервер запущен");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ServerApp().run();
    }
}
