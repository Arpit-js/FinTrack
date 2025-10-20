package com.fynance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    // Add a new group
    public void addGroup(ExpenseGroup group) {
        String query = "INSERT INTO expense_groups (group_name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, group.getGroupName());
            stmt.executeUpdate();
            System.out.println("✅ Group added successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Error adding group: " + e.getMessage());
        }
    }

    public void updateGroup(ExpenseGroup group) {
        String query = "UPDATE expense_groups SET group_name = ? WHERE group_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, group.getGroupName());
            stmt.setInt(2, group.getGroupId());
            stmt.executeUpdate();
            System.out.println("✅ Group updated successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Error updating group: " + e.getMessage());
        }
    }

    // Fetch a single group by ID
    public ExpenseGroup getGroupById(int id) {
        ExpenseGroup group = null;
        String query = "SELECT * FROM expense_groups WHERE group_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                group = new ExpenseGroup();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching group: " + e.getMessage());
        }
        return group;
    }

    // Fetch all groups
    public List<ExpenseGroup> getAllGroups() {
        List<ExpenseGroup> groups = new ArrayList<>();
        String query = "SELECT * FROM expense_groups";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ExpenseGroup group = new ExpenseGroup();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                groups.add(group);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching groups: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Deletes a group and all its associated data (members, expenses, splits,
     * settlements). This is done in a transaction to ensure data integrity.
     */
    public void deleteGroup(int groupId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete Settlements
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM settlements WHERE group_id = ?")) {
                stmt.setInt(1, groupId);
                stmt.executeUpdate();
            }

            // 2. Get all expense IDs for the group
            List<Integer> expenseIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT expense_id FROM expenses WHERE group_id = ?")) {
                stmt.setInt(1, groupId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    expenseIds.add(rs.getInt("expense_id"));
                }
            }

            // 3. Delete Expense Splits for each expense
            if (!expenseIds.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM expense_splits WHERE expense_id = ?")) {
                    for (int expenseId : expenseIds) {
                        stmt.setInt(1, expenseId);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            // 4. Delete Expenses
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM expenses WHERE group_id = ?")) {
                stmt.setInt(1, groupId);
                stmt.executeUpdate();
            }

            // 5. Delete Group Members
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM group_members WHERE group_id = ?")) {
                stmt.setInt(1, groupId);
                stmt.executeUpdate();
            }

            // 6. Finally, Delete the Group
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM expense_groups WHERE group_id = ?")) {
                stmt.setInt(1, groupId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Group and all associated data deleted successfully!");
                } else {
                    System.out.println("⚠️ No group found with ID: " + groupId);
                }
            }

            conn.commit(); // Commit the transaction

        } catch (SQLException e) {
            System.out.println("❌ Error deleting group: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
