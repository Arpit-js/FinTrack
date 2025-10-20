package com.fynance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberDAO {

    public void addMemberToGroup(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("✅ User added to group successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // In GroupMemberDAO.java

    public void removeMemberFromGroup(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // In GroupMemberDAO.java

    public List<ExpenseGroup> getGroupsForUser(int userId) {
        List<ExpenseGroup> groups = new java.util.ArrayList<>();
        String sql = "SELECT eg.group_id, eg.group_name FROM expense_groups eg "
                + "JOIN group_members gm ON eg.group_id = gm.group_id "
                + "WHERE gm.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(new ExpenseGroup(rs.getInt("group_id"), rs.getString("group_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<GroupMember> getMembersByGroup(int groupId) {
        List<GroupMember> members = new ArrayList<>();
        String sql = "SELECT * FROM group_members WHERE group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                GroupMember member = new GroupMember(
                        rs.getInt("id"),
                        rs.getInt("group_id"),
                        rs.getInt("user_id")
                );
                members.add(member);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
}
