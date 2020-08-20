package com.geekbrains.donni.storage.cloud.database;

public interface AuthService {
    boolean getUsernameByLoginAndPassword(String login, String password);

    void start();
    void stop();
}
