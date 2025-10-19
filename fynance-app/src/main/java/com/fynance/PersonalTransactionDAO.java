package com.fynance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalTransactionDAO {

    public void addTransaction(PersonalTransaction transaction) {
        String sql = "INSERT INTO personal_transactions (user_id, type, amount, category, transaction_date, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getUserId());
            stmt.setString(2, transaction.getType());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setString(4, transaction.getCategory());
            stmt.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            stmt.setString(6, transaction.getNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Add this method to your PersonalTransactionDAO.java file

    public Map<String, Double> getMonthlyExpenseSummary(int userId, int numberOfMonths) {
        Map<String, Double> summary = new HashMap<>();
        // This query gets the total expenses for each of the last few months
        String sql = "SELECT YEAR(transaction_date) as year, MONTH(transaction_date) as month, SUM(amount) as total "
                + "FROM personal_transactions "
                + "WHERE user_id = ? AND type = 'EXPENSE' AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) "
                + "GROUP BY YEAR(transaction_date), MONTH(transaction_date) "
                + "ORDER BY year, month";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, numberOfMonths);
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
            while (rs.next()) {
                LocalDate date = LocalDate.of(rs.getInt("year"), rs.getInt("month"), 1);
                String monthName = date.format(formatter);
                summary.put(monthName, rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    public List<PersonalTransaction> getTransactionsForUserByMonth(int userId, int month, int year) {
        List<PersonalTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM personal_transactions WHERE user_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PersonalTransaction t = new PersonalTransaction(
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getDate("transaction_date").toLocalDate(),
                        rs.getString("notes")
                );
                t.setTransactionId(rs.getInt("transaction_id"));
                transactions.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<PersonalTransaction> getAllTransactionsForUser(int userId) {
        List<PersonalTransaction> transactions = new ArrayList<>();
        // Order by date in descending order to show the latest first
        String sql = "SELECT * FROM personal_transactions WHERE user_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PersonalTransaction t = new PersonalTransaction(
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getDate("transaction_date").toLocalDate(),
                        rs.getString("notes")
                );
                t.setTransactionId(rs.getInt("transaction_id"));
                transactions.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
