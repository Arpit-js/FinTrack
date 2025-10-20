package com.fynance.analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fynance.DatabaseConnection;
import com.fynance.PersonalTransaction;

public class AnalyticsDAO {

    public Map<String, Double> getExpensesByCategory(int userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> categoryExpenses = new LinkedHashMap<>();
        String sql = "SELECT c.name, SUM(pt.amount) as total "
                + "FROM personal_transactions pt "
                + "JOIN categories c ON pt.category_id = c.category_id "
                + "WHERE pt.user_id = ? AND pt.transaction_date BETWEEN ? AND ? AND pt.type = 'EXPENSE' "
                + "GROUP BY c.name "
                + "ORDER BY total DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categoryExpenses.put(rs.getString("name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryExpenses;
    }

    public Map<String, Double> getMonthlyExpenseSummary(int userId, int months) {
        Map<String, Double> monthlyTotals = new LinkedHashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        // CORRECTED SQL: Using DATE_FORMAT for MySQL instead of strftime
        String sql = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') as month, SUM(amount) as total "
                + "FROM personal_transactions "
                + "WHERE user_id = ? AND transaction_date BETWEEN ? AND ? AND type = 'EXPENSE' "
                + "GROUP BY month "
                + "ORDER BY month";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                monthlyTotals.put(rs.getString("month"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlyTotals;
    }
    // In AnalyticsDAO.java, add this new method

    public Map<LocalDate, Double> getDailyExpensesForCurrentMonth(int userId) {
        Map<LocalDate, Double> dailyExpenses = new TreeMap<>();
        // Fetches expenses for the current month, grouped by date
        String sql = "SELECT transaction_date, SUM(amount) as total "
                + "FROM personal_transactions "
                + "WHERE user_id = ? AND type = 'EXPENSE' AND "
                + "DATE_FORMAT(transaction_date, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') "
                + "GROUP BY transaction_date "
                + "ORDER BY transaction_date";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                dailyExpenses.put(rs.getDate("transaction_date").toLocalDate(), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailyExpenses;
    }

    public List<PersonalTransaction> getTopNExpenses(int userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<PersonalTransaction> topExpenses = new ArrayList<>();
        String sql = "SELECT * FROM personal_transactions "
                + "WHERE user_id = ? AND transaction_date BETWEEN ? AND ? AND type = 'EXPENSE' "
                + "ORDER BY amount DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            pstmt.setInt(4, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // This call now works because you added the new constructor to PersonalTransaction.java
                topExpenses.add(new PersonalTransaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getInt("category_id"),
                        rs.getDate("transaction_date").toLocalDate(),
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topExpenses;
    }
}
