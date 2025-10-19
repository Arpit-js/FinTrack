package com.fynance;

import java.time.LocalDate;

public class PersonalTransaction {

    private int transactionId;
    private int userId;
    private String type;
    private double amount;
    private String category;
    private LocalDate transactionDate;
    private String notes;

    public PersonalTransaction(int userId, String type, double amount, String category, LocalDate transactionDate, String notes) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    // --- Getters ---
    public int getTransactionId() {
        return transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    // --- Setters ---
    public void setTransactionId(int id) {
        this.transactionId = id;
    }
}
