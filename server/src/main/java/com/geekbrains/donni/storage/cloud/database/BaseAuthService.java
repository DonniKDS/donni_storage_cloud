package com.geekbrains.donni.storage.cloud.database;

import java.sql.*;

public class BaseAuthService implements AuthService {

    private static Connection connection;
    private static Statement statement;

    @Override
    public boolean getUsernameByLoginAndPassword(String login, String password) {
        String query = String.format("SELECT id FROM users WHERE login='%s' AND pass='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next()){
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключенния к базе данных.");
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/users.db");
            statement = connection.createStatement();
            System.out.println("База данных подключена");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Ошибка подключенния к базе данных.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Ошибка подключенния к базе данных.");
            throw new RuntimeException(e);
        }
    }
}
