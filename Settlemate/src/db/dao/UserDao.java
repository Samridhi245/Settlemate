package db.dao;

import db.DatabaseManager;
import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final DatabaseManager databaseManager;

    public UserDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void upsert(User user) throws SQLException {
        String sql = "INSERT INTO users (user_id, name, email) VALUES (?, ?, ?) " + "ON CONFLICT(user_id) DO UPDATE SET name=excluded.name, email=excluded.email";
        try (Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.executeUpdate();
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT user_id, name, email FROM users");
             ResultSet rs = statement.executeQuery()) {
            
            while (rs.next()) {
                users.add(new User(rs.getString("user_id"), rs.getString("name"), rs.getString("email")));
            }
        }
        return users;
    }

    public void deleteById(String userId) throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            statement.setString(1, userId);
            statement.executeUpdate();
        }
    }
}
