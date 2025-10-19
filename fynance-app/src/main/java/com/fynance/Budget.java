package com.fynance;

public class Budget {

    private int budgetId;
    private int userId;
    private int month;
    private int year;
    private double budgetAmount;

    public Budget(int userId, int month, int year, double budgetAmount) {
        this.userId = userId;
        this.month = month;
        this.year = year;
        this.budgetAmount = budgetAmount;
    }

    // --- Getters ---
    public int getUserId() {
        return userId;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }
}
