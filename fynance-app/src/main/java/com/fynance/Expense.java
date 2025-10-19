package com.fynance;

public class Expense {

    private int expenseId;
    private int groupId;
    private int paidByUserId;
    private double amount;
    private String description;
    private String category;

    public Expense() {
    }

    public Expense(int expenseId, int groupId, int paidByUserId, double amount, String description, String category) {
        this.expenseId = expenseId;
        this.groupId = groupId;
        this.paidByUserId = paidByUserId;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    public Expense(int groupId, int paidByUserId, double amount, String description, String category) {
        this.groupId = groupId;
        this.paidByUserId = paidByUserId;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    // --- Getters ---
    public int getExpenseId() {
        return expenseId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getPaidByUserId() {
        return paidByUserId;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    // --- Setters ---
    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setPaidByUserId(int paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Expense{" + "expenseId=" + expenseId + ", groupId=" + groupId + ", paidByUserId=" + paidByUserId
                + ", amount=" + amount + ", description='" + description + '\'' + ", category='" + category + '\'' + '}';
    }
}
