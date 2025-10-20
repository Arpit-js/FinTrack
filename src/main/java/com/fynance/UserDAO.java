package com.fynance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void addUser(User user) {
        String query = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET name = ?, email = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserById(int id) {
        User user = null;
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void deleteUser(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Remove user from all groups
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM group_members WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
            // Remove from expense splits
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM expense_splits WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
            // Remove settlements where user is payer or receiver
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM settlements WHERE from_user_id = ? OR to_user_id = ?")) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
            // Note: We are not deleting expenses paid by the user to preserve group history.
            // A more complex app might reassign them or mark them as paid by "Deleted User".

            // Finally, delete the user
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
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
