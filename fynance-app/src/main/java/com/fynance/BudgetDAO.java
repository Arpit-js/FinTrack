package com.fynance;

import java.sql.*;

public class BudgetDAO {

    public void setBudget(Budget budget) {
        // Use ON DUPLICATE KEY UPDATE to either insert a new budget or update the existing one for the month
        String sql = "INSERT INTO budgets (user_id, month, year, budget_amount) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE budget_amount = VALUES(budget_amount)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budget.getUserId());
            stmt.setInt(2, budget.getMonth());
            stmt.setInt(3, budget.getYear());
            stmt.setDouble(4, budget.getBudgetAmount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Budget getBudgetForUser(int userId, int month, int year) {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND month = ? AND year = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Budget(
                        rs.getInt("user_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getDouble("budget_amount")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no budget is set
    }
}
