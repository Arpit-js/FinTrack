package com.fynance;
// ExpenseSplit.java

public class ExpenseSplit {

    private int id;
    private int expenseId;
    private int userId;
    private double amount;        // absolute rupee amount
    private Double percentage;    // nullable; if present shows percent used to derive amount

    public ExpenseSplit() {
    }

    public ExpenseSplit(int expenseId, int userId, double amount, Double percentage) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.amount = amount;
        this.percentage = percentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public int getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public Double getPercentage() {
        return percentage;
    }

    @Override
    public String toString() {
        return "ExpenseSplit{" + "id=" + id + ", expenseId=" + expenseId + ", userId=" + userId
                + ", amount=" + amount + ", percentage=" + percentage + '}';
    }
}
