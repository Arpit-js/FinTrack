package com.fynance.analytics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.fynance.Budget;
import com.fynance.CategoryDAO;
import com.fynance.PersonalTransaction;
import com.fynance.PieChartPanel;
import com.fynance.RoundedPanel;
import com.fynance.UITheme;

public class AnalyticsPanel extends JPanel {

    private final AnalyticsService analyticsService;
    private final int currentUserId = 1;

    private final JLabel budgetLabel = new JLabel("₹0.00");
    private final JLabel expensesLabel = new JLabel("₹0.00");
    private final JLabel remainingLabel = new JLabel("₹0.00");
    private final JLabel incomeLabel = new JLabel("₹0.00");
    private final JLabel savingsLabel = new JLabel("₹0.00");

    private final PieChartPanel pieChartPanel;
    // *** 1. Replace BarChartPanel with our new DailyExpenseLineChart ***
    private final DailyExpenseLineChart dailyExpenseLineChart;
    private final JTable topExpensesTable;
    private final DefaultTableModel tableModel;

    public AnalyticsPanel() {
        this.analyticsService = new AnalyticsService();
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND_DARK);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Panel with metric tiles
        JPanel topPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        topPanel.setOpaque(false);
        topPanel.add(createMetricCard("Monthly Budget", budgetLabel));
        topPanel.add(createMetricCard("Month-to-Date Expenses", expensesLabel));
        topPanel.add(createMetricCard("Remaining Budget", remainingLabel));
        topPanel.add(createMetricCard("Income This Month", incomeLabel));
        topPanel.add(createMetricCard("Savings This Month", savingsLabel));
        add(topPanel, BorderLayout.NORTH);

        // Center Panel with charts and table
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setOpaque(false);

        // Left side with charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chartsPanel.setOpaque(false);
        pieChartPanel = new PieChartPanel();
        chartsPanel.add(createChartContainer("Expense by Category", pieChartPanel));

        // *** 2. Instantiate the new chart and update the title ***
        dailyExpenseLineChart = new DailyExpenseLineChart();
        chartsPanel.add(createChartContainer("Daily Expense Trend (This Month)", dailyExpenseLineChart));
        centerPanel.add(chartsPanel);

        // Right side with top expenses table
        tableModel = new DefaultTableModel(new String[]{"Date", "Category", "Amount", "Note"}, 0);
        topExpensesTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(topExpensesTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Top 5 Expenses This Month"));
        centerPanel.add(tableScrollPane);

        add(centerPanel, BorderLayout.CENTER);

        refreshAnalyticsData();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        RoundedPanel card = new RoundedPanel(15);
        card.setBackground(UITheme.PRIMARY_NAVY);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_MAIN);
        titleLabel.setForeground(UITheme.TEXT_LIGHT_GRAY);
        valueLabel.setFont(UITheme.FONT_HEADER);
        valueLabel.setForeground(UITheme.ACCENT_PINK);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartContainer(String title, JComponent chartComponent) {
        RoundedPanel container = new RoundedPanel(15);
        container.setBackground(UITheme.PRIMARY_NAVY);
        container.setLayout(new BorderLayout());
        container.setBorder(BorderFactory.createTitledBorder(title));
        container.add(chartComponent, BorderLayout.CENTER);
        return container;
    }

    public void refreshAnalyticsData() {
        new SwingWorker<Void, Void>() {
            private Map<String, Double> categoryData;
            // *** 3. Add a variable for our new daily data ***
            private Map<LocalDate, Double> dailyData;
            private List<PersonalTransaction> topExpenses;
            private double income;
            private double savings;
            private Budget budget;
            private double mtdExpenses;

            @Override
            protected Void doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                LocalDate startDate = today.withDayOfMonth(1);
                categoryData = analyticsService.getExpensesByCategory(currentUserId, startDate, today);
                // *** 4. Fetch the new daily data from the service ***
                dailyData = analyticsService.getDailyExpensesForCurrentMonth(currentUserId);
                topExpenses = analyticsService.getTopNExpenses(currentUserId, startDate, today, 5);
                income = analyticsService.getIncomeThisMonth(currentUserId);
                savings = analyticsService.getSavingsThisMonth(currentUserId);
                budget = analyticsService.getBudgetThisMonth(currentUserId);
                mtdExpenses = categoryData.values().stream().mapToDouble(Double::doubleValue).sum();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // To catch any exceptions
                    pieChartPanel.setData(categoryData);
                    // *** 5. Set the data on our new line chart ***
                    dailyExpenseLineChart.setData(dailyData);

                    tableModel.setRowCount(0);
                    CategoryDAO categoryDAO = new CategoryDAO();
                    for (PersonalTransaction t : topExpenses) {
                        Vector<Object> row = new Vector<>();
                        row.add(t.getTransactionDate());
                        row.add(categoryDAO.getCategoryById(t.getCategoryId()).getName());
                        row.add(t.getAmount());
                        row.add(t.getNotes());
                        tableModel.addRow(row);
                    }

                    incomeLabel.setText(String.format("₹%.2f", income));
                    savingsLabel.setText(String.format("₹%.2f", savings));
                    expensesLabel.setText(String.format("₹%.2f", mtdExpenses));

                    if (budget != null) {
                        budgetLabel.setText(String.format("₹%.2f", budget.getBudgetAmount()));
                        double remaining = budget.getBudgetAmount() - mtdExpenses;
                        remainingLabel.setText(String.format("₹%.2f", remaining));
                        remainingLabel.setForeground(remaining >= 0 ? UITheme.ACCENT_PINK : Color.RED);
                    } else {
                        budgetLabel.setText("Not Set");
                        remainingLabel.setText("N/A");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AnalyticsPanel.this, "Error loading analytics data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
