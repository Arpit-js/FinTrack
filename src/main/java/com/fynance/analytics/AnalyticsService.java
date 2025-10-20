package com.fynance.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fynance.Budget;
import com.fynance.BudgetDAO;
import com.fynance.PersonalTransaction;
import com.fynance.PersonalTransactionDAO;

public class AnalyticsService {

    private final AnalyticsDAO analyticsDAO;
    private final PersonalTransactionDAO personalTransactionDAO;
    private final BudgetDAO budgetDAO;

    public AnalyticsService() {
        this.analyticsDAO = new AnalyticsDAO();
        this.personalTransactionDAO = new PersonalTransactionDAO();
        this.budgetDAO = new BudgetDAO();
    }

    public Map<String, Double> getExpensesByCategory(int userId, LocalDate startDate, LocalDate endDate) {
        return analyticsDAO.getExpensesByCategory(userId, startDate, endDate);
    }

    public Map<String, Double> getMonthlyExpenseSummary(int userId, int months) {
        return analyticsDAO.getMonthlyExpenseSummary(userId, months);
    }

    public List<PersonalTransaction> getTopNExpenses(int userId, LocalDate startDate, LocalDate endDate, int limit) {
        return analyticsDAO.getTopNExpenses(userId, startDate, endDate, limit);
    }
    // In AnalyticsService.java, add this new method

    public Map<LocalDate, Double> getDailyExpensesForCurrentMonth(int userId) {
        return analyticsDAO.getDailyExpensesForCurrentMonth(userId);
    }

    public double getIncomeThisMonth(int userId) {
        LocalDate today = LocalDate.now();
        List<PersonalTransaction> transactions = personalTransactionDAO.getTransactionsForUserByMonth(userId, today.getMonthValue(), today.getYear());
        return transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .mapToDouble(PersonalTransaction::getAmount)
                .sum();
    }

    public double getSavingsThisMonth(int userId) {
        LocalDate today = LocalDate.now();
        List<PersonalTransaction> transactions = personalTransactionDAO.getTransactionsForUserByMonth(userId, today.getMonthValue(), today.getYear());
        return transactions.stream()
                .filter(t -> "SAVING".equals(t.getType()))
                .mapToDouble(PersonalTransaction::getAmount)
                .sum();
    }

    public Budget getBudgetThisMonth(int userId) {
        LocalDate today = LocalDate.now();
        return budgetDAO.getBudgetForUser(userId, today.getMonthValue(), today.getYear());
    }
}
