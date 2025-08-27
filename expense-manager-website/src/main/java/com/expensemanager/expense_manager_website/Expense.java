package com.expensemanager.expense_manager_website;

public class Expense {
    private String description;
    private String category;
    private double amount;

    public Expense(String description, String category, double amount) {
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}
