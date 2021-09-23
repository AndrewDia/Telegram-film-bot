package com.example.filmbot;

import com.example.filmbot.entity.Chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DBManager {

    private static DBManager dbManager;
    private Connection connection;

    private DBManager() {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream("app.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String connectionURL = properties.getProperty("connection.url");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(connectionURL, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DBManager getInstance() {
        if (dbManager == null)
            dbManager = new DBManager();
        return dbManager;
    }

    public Chat getChat(long chatId) {
        Chat chat = null;
        ResultSet resultSet = null;
        try (Statement statement = connection.createStatement()) {
            resultSet = statement.executeQuery("SELECT * FROM chats WHERE chat_id = " + chatId);
            if (resultSet.next())
                chat = mapChat(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return chat;
    }

    public void insertChat(Chat chat) {
        String sql = "INSERT INTO chats(chat_id, film_num, sorting_way, chosen_period) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, chat.getChatId());
            ps.setInt(2, chat.getFilmNum());
            ps.setString(3, chat.getSortingWay());
            ps.setString(4, chat.getChosenPeriod());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateChat(Chat chat) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE chats SET film_num = ?, sorting_way = ?, chosen_period = ? WHERE chat_id = ?")) {
            preparedStatement.setInt(1, chat.getFilmNum());
            preparedStatement.setString(2, chat.getSortingWay());
            preparedStatement.setString(3, chat.getChosenPeriod());
            preparedStatement.setLong(4, chat.getChatId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteChat(Chat chat) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM chats WHERE chat_id = ?")) {
            preparedStatement.setLong(1, chat.getChatId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private Chat mapChat(ResultSet resultSet) throws SQLException {
        Chat chat = new Chat(resultSet.getInt(1));
        chat.setFilmNum(resultSet.getInt(2));
        chat.setSortingWay(resultSet.getString(3));
        chat.setChosenPeriod(resultSet.getString(4));
        return chat;
    }
}
