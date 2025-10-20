package com.fynance;

import java.time.LocalDate;

public class PersonalTransaction {

    private int transactionId;
    private int userId;
    private String type; // e.g., "EXPENSE", "INCOME", "SAVING"
    private double amount;
    private int categoryId;
    private LocalDate transactionDate;
    private String notes;

    // Constructor for creating a new transaction object (e.g., before saving to DB)
    public PersonalTransaction(int userId, String type, double amount, int categoryId, LocalDate transactionDate, String notes) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.categoryId = categoryId;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    /**
     * ADD THIS CONSTRUCTOR This new constructor is used for creating
     * transaction objects from data that has been retrieved from the database,
     * which includes the transactionId.
     */
    public PersonalTransaction(int transactionId, int userId, String type, double amount, int categoryId, LocalDate transactionDate, String notes) {
        // Calls the other constructor to set the common fields
        this(userId, type, amount, categoryId, transactionDate, notes);
        // Sets the transaction ID
        this.transactionId = transactionId;
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
