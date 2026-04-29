package db.dao;

import db.DatabaseManager;
import models.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class GroupDao {
    private final DatabaseManager databaseManager;

    public GroupDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void upsert(Group group) throws SQLException {
        String sql = "INSERT INTO groups (group_id, group_name) VALUES (?, ?) " +
                "ON CONFLICT(group_id) DO UPDATE SET group_name=excluded.group_name";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, group.getGroupId());
            statement.setString(2, group.getGroupName());
            statement.executeUpdate();
        }
        replaceMembers(group.getGroupId(), group.getMemberUserIds());
    }

    public void replaceMembers(String groupId, List<String> memberUserIds) throws SQLException {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM group_members WHERE group_id = ?");
                 PreparedStatement insertStatement = connection.prepareStatement(
                         "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)")) {
                deleteStatement.setString(1, groupId);
                deleteStatement.executeUpdate();
                for (String memberId : memberUserIds) {
                    insertStatement.setString(1, groupId);
                    insertStatement.setString(2, memberId);
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
            connection.commit();
        }
    }

    public void deleteById(String groupId) throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM groups WHERE group_id = ?")) {
            statement.setString(1, groupId);
            statement.executeUpdate();
        }
    }
}
