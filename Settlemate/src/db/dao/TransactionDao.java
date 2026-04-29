package db.dao;

import db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    private final DatabaseManager databaseManager;

    public TransactionDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(String groupId, String transactionType, String referenceId, String details, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (group_id, transaction_type, reference_id, details, amount, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, groupId);
            statement.setString(2, transactionType);
            statement.setString(3, referenceId);
            statement.setString(4, details);
            statement.setDouble(5, amount);
            statement.setString(6, LocalDateTime.now().toString());
            statement.executeUpdate();
        }
    }

    public List<String> getHistoryByGroup(String groupId) throws SQLException {
        String sql = "SELECT transaction_type, details, amount, created_at FROM transactions " +
                "WHERE group_id = ? ORDER BY transaction_id DESC";
        List<String> rows = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, groupId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String row = rs.getString("created_at") + " | " +
                            rs.getString("transaction_type") + " | " +
                            String.format("%.2f", rs.getDouble("amount")) + " | " +
                            rs.getString("details");
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
