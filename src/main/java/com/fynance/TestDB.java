package com.fynance;

import java.sql.Connection;

public class TestDB {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Database connected successfully!");
            } else {
                System.out.println("❌ Failed to connect to database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
