package db.dao;

import db.DatabaseManager;
import models.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class ExpenseDao {
    private final DatabaseManager databaseManager;

    public ExpenseDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insertExpenseWithSplits(Expense expense) throws SQLException {
        String expenseSql = "INSERT INTO expenses " +
                "(expense_id, group_id, description, amount, paid_by_user_id, split_type, category, expense_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String splitSql = "INSERT INTO expense_splits (expense_id, user_id, share_amount) VALUES (?, ?, ?)";

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement expenseStatement = connection.prepareStatement(expenseSql);
                 
                PreparedStatement splitStatement = connection.prepareStatement(splitSql)) {
                expenseStatement.setString(1, expense.getExpenseId());
                expenseStatement.setString(2, expense.getGroupId());
                expenseStatement.setString(3, expense.getDescription());
                expenseStatement.setDouble(4, expense.getAmount());
                expenseStatement.setString(5, expense.getPaidByUserId());
                expenseStatement.setString(6, expense.getSplitType().name());
                expenseStatement.setString(7, expense.getCategory());
                expenseStatement.setString(8, expense.getDate().toString());
                expenseStatement.executeUpdate();

                for (Map.Entry<String, Double> entry : expense.getSplitDetails().entrySet()) {
                    splitStatement.setString(1, expense.getExpenseId());
                    splitStatement.setString(2, entry.getKey());
                    splitStatement.setDouble(3, entry.getValue());
                    splitStatement.addBatch();
                }
                splitStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void deleteById(String expenseId) throws SQLException {
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM expenses WHERE expense_id = ?")) {
            statement.setString(1, expenseId);
            statement.executeUpdate();
        }
    }
}
