package db.dao;

import db.DatabaseManager;
import models.SettlementTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SettlementDao {
    private final DatabaseManager databaseManager;

    public SettlementDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insertSettlements(String groupId, List<SettlementTransaction> settlements) throws SQLException {
        String sql = "INSERT INTO settlements (group_id, from_user_id, to_user_id, amount, settled_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String now = LocalDateTime.now().toString();
            for (SettlementTransaction settlement : settlements) {
                statement.setString(1, groupId);
                statement.setString(2, settlement.getFromUserId());
                statement.setString(3, settlement.getToUserId());
                statement.setDouble(4, settlement.getAmount());
                statement.setString(5, now);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
