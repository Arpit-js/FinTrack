package com.fynance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SettlementDAO {

    public void addSettlement(Settlement settlement) {
        String sql = "INSERT INTO settlements (group_id, from_user_id, to_user_id, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, settlement.getGroupId());
            stmt.setInt(2, settlement.getFromUserId());
            stmt.setInt(3, settlement.getToUserId());
            stmt.setDouble(4, settlement.getAmount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Settlement> getSettlementsForGroup(int groupId) {
        List<Settlement> settlements = new ArrayList<>();
        String sql = "SELECT * FROM settlements WHERE group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                settlements.add(new Settlement(
                        rs.getInt("group_id"),
                        rs.getInt("from_user_id"),
                        rs.getInt("to_user_id"),
                        rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settlements;
    }
}
