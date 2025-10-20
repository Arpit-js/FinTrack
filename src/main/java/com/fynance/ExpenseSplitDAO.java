package com.fynance;
// ExpenseSplitDAO.java

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ExpenseSplitDAO {

    public void addExpenseSplits(List<ExpenseSplit> splits) throws SQLException {
        String sql = "INSERT INTO expense_splits (expense_id, user_id, amount, percentage) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ExpenseSplit s : splits) {
                stmt.setInt(1, s.getExpenseId());
                stmt.setInt(2, s.getUserId());
                stmt.setDouble(3, s.getAmount());
                if (s.getPercentage() == null) {
                    stmt.setNull(4, Types.DOUBLE);
                } else {
                    stmt.setDouble(4, s.getPercentage());
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<ExpenseSplit> getSplitsByExpense(int expenseId) {
        List<ExpenseSplit> result = new ArrayList<>();
        String sql = "SELECT * FROM expense_splits WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ExpenseSplit s = new ExpenseSplit();
                s.setId(rs.getInt("id"));
                // set fields via reflection not needed; use constructor
                // but we set manually
                s = new ExpenseSplit(rs.getInt("expense_id"), rs.getInt("user_id"), rs.getDouble("amount"),
                        (rs.getObject("percentage") != null) ? rs.getDouble("percentage") : null);
                s.setId(rs.getInt("id"));
                result.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
