import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Random;

class Expense {
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

    @Override
    public String toString() {
        return "Description: " + description + ", Category: " + category + ", Amount: " + amount;
    }
}

public class ExpenseManager {
    private ArrayList<Expense> expenses = new ArrayList<>();
    private HashMap<String, Double> categoryBudgets = new HashMap<>();
    private HashMap<String, Double> categoryTotals = new HashMap<>();
    private HashMap<String, Double> lastMonthTotals = new HashMap<>();
    private Random random = new Random();

    public void addExpense(String description, String category, double amount) {
        Expense expense = new Expense(description, category, amount);
        expenses.add(expense);
        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);

        System.out.println("Expense added successfully.");

        // Check if the expense exceeds the budget
        if (categoryBudgets.containsKey(category)) {
            double total = categoryTotals.get(category);
            if (total > categoryBudgets.get(category)) {
                System.out.println("ALERT: You have exceeded the budget for " + category + "!");
            }
        }

        // Anomaly detection
        detectAnomaly(category, amount);
    }

    public void setBudget(String category, double budget) {
        categoryBudgets.put(category, budget);
        System.out.println("Budget set for category: " + category);
    }

    public void viewExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
        } else {
            System.out.println("\n--- List of Expenses ---");
            for (Expense expense : expenses) {
                System.out.println(expense);
            }
            System.out.println("\n--- Category Totals ---");
            for (String category : categoryTotals.keySet()) {
                System.out.println(category + ": " + categoryTotals.get(category) +
                        " (Budget: " + categoryBudgets.getOrDefault(category, 0.0) + ")");
            }
        }
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            Expense removedExpense = expenses.remove(index);
            String category = removedExpense.getCategory();
            categoryTotals.put(category, categoryTotals.get(category) - removedExpense.getAmount());
            System.out.println("Expense deleted successfully.");
        } else {
            System.out.println("Invalid index. Please try again.");
        }
    }

    public void analyzeSpendingPatterns() {
        System.out.println("\n--- Spending Patterns ---");
        for (String category : categoryTotals.keySet()) {
            double total = categoryTotals.get(category);
            double budget = categoryBudgets.getOrDefault(category, 0.0);
            if (budget > 0) {
                double percentage = (total / budget) * 100;
                System.out.printf("%s: %.2f%% of budget spent\n", category, percentage);
            } else {
                System.out.println(category + ": No budget set");
            }
        }
    }

    private void detectAnomaly(String category, double amount) {
        double average = lastMonthTotals.getOrDefault(category, 0.0) / 30;
        if (amount > 2 * average) {
            System.out.println("ANOMALY ALERT: Unusually high spending detected in " + category + "!");
        }
    }

    public void suggestBudgetAdjustments() {
        System.out.println("\n--- Budget Adjustment Suggestions ---");
        for (String category : categoryTotals.keySet()) {
            double total = categoryTotals.get(category);
            double budget = categoryBudgets.getOrDefault(category, 0.0);
            if (budget > 0 && total > budget * 1.2) {
                System.out.println("Consider increasing the budget for " + category + " as spending often exceeds it.");
            } else if (budget > 0 && total < budget * 0.5) {
                System.out.println("You could decrease the budget for " + category + " to save more.");
            }
        }
    }

    public void predictNextMonthExpenses() {
        System.out.println("\n--- Predicted Expenses for Next Month ---");
        for (String category : categoryTotals.keySet()) {
            double predictedTotal = categoryTotals.get(category)
                    + (random.nextDouble() * 0.1 * categoryTotals.get(category));
            System.out.printf("%s: %.2f\n", category, predictedTotal);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExpenseManager expenseManager = new ExpenseManager();

        while (true) {
            System.out.println("\n--- Personal Expense Manager ---");
            System.out.println("1. Add Expense");
            System.out.println("2. View Expenses");
            System.out.println("3. Delete Expense");
            System.out.println("4. Set Budget for a Category");
            System.out.println("5. Analyze Spending Patterns");
            System.out.println("6. Suggest Budget Adjustments");
            System.out.println("7. Predict Next Month's Expenses");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter description: ");
                    String description = scanner.nextLine();
                    System.out.print("Enter category: ");
                    String category = scanner.nextLine();
                    System.out.print("Enter amount: ");
                    double amount = scanner.nextDouble();
                    expenseManager.addExpense(description, category, amount);
                    break;
                case 2:
                    expenseManager.viewExpenses();
                    break;
                case 3:
                    System.out.print("Enter expense number to delete: ");
                    int index = scanner.nextInt() - 1;
                    expenseManager.deleteExpense(index);
                    break;
                case 4:
                    System.out.print("Enter category for budget: ");
                    String budgetCategory = scanner.nextLine();
                    System.out.print("Enter budget amount: ");
                    double budgetAmount = scanner.nextDouble();
                    expenseManager.setBudget(budgetCategory, budgetAmount);
                    break;
                case 5:
                    expenseManager.analyzeSpendingPatterns();
                    break;
                case 6:
                    expenseManager.suggestBudgetAdjustments();
                    break;
                case 7:
                    expenseManager.predictNextMonthExpenses();
                    break;
                case 8:
                    System.out.println("Exiting the program. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
