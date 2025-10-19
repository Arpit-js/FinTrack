package com.fynance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public void updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET amount = ?, description = ?, category = ? WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, expense.getAmount());
            stmt.setString(2, expense.getDescription());
            stmt.setString(3, expense.getCategory());
            stmt.setInt(4, expense.getExpenseId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ... rest of the file is the same ...
    // Add a new expense
    public void addExpense(Expense expense) {
        String query = "INSERT INTO expenses (description, amount, paid_by_user_id, group_id, category) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, expense.getDescription());
            stmt.setDouble(2, expense.getAmount());
            stmt.setInt(3, expense.getPaidByUserId());
            stmt.setInt(4, expense.getGroupId());
            stmt.setString(5, expense.getCategory());
            stmt.executeUpdate();

            System.out.println("✅ Expense added successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Error adding expense: " + e.getMessage());
        }
    }

    public void addExpenseWithSplits(Expense expense, List<ExpenseSplit> splits) throws SQLException {
        String expenseSQL = "INSERT INTO expenses (group_id, paid_by_user_id, amount, description, category) VALUES (?, ?, ?, ?, ?)";
        String splitSQL = "INSERT INTO expense_splits (expense_id, user_id, amount, percentage) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Add expense and get generated ID
            try (PreparedStatement expenseStmt = conn.prepareStatement(expenseSQL, Statement.RETURN_GENERATED_KEYS)) {
                expenseStmt.setInt(1, expense.getGroupId());
                expenseStmt.setInt(2, expense.getPaidByUserId());
                expenseStmt.setDouble(3, expense.getAmount());
                expenseStmt.setString(4, expense.getDescription());
                expenseStmt.setString(5, expense.getCategory());
                expenseStmt.executeUpdate();

                try (ResultSet generatedKeys = expenseStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int expenseId = generatedKeys.getInt(1);
                        // Add splits
                        try (PreparedStatement splitStmt = conn.prepareStatement(splitSQL)) {
                            for (ExpenseSplit split : splits) {
                                splitStmt.setInt(1, expenseId);
                                splitStmt.setInt(2, split.getUserId());
                                splitStmt.setDouble(3, split.getAmount());
                                if (split.getPercentage() != null) {
                                    splitStmt.setDouble(4, split.getPercentage());
                                } else {
                                    splitStmt.setNull(4, java.sql.Types.DOUBLE);
                                }
                                splitStmt.addBatch();
                            }
                            splitStmt.executeBatch();
                        }
                    }
                }
            }
            conn.commit();
            System.out.println("✅ Expense with splits added successfully!");
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // Fetch a single expense by ID
    public Expense getExpenseById(int id) {
        Expense expense = null;
        String query = "SELECT * FROM expenses WHERE expense_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                expense = new Expense(
                        rs.getInt("expense_id"),
                        rs.getInt("group_id"),
                        rs.getInt("paid_by_user_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("category")
                );
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching expense: " + e.getMessage());
        }
        return expense;
    }

    // Fetch all expenses
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        String query = "SELECT * FROM expenses";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Expense expense = new Expense(
                        rs.getInt("expense_id"),
                        rs.getInt("group_id"),
                        rs.getInt("paid_by_user_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("category")
                );
                expenses.add(expense);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching expenses: " + e.getMessage());
        }
        return expenses;
    }

    // Fetch all expenses for a given group
    public List<Expense> getExpensesByGroup(int groupId) {
        List<Expense> expenses = new ArrayList<>();
        String query = "SELECT * FROM expenses WHERE group_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Expense expense = new Expense(
                        rs.getInt("expense_id"),
                        rs.getInt("group_id"),
                        rs.getInt("paid_by_user_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("category")
                );
                expenses.add(expense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }
}
