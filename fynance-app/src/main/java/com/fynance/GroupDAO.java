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

    // Delete a group by ID
    public void deleteGroup(int id) {
        String query = "DELETE FROM expense_groups WHERE group_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Group deleted successfully!");
            } else {
                System.out.println("⚠️ No group found with ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error deleting group: " + e.getMessage());
        }
    }
}
