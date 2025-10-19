package com.fynance;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainApp {

    private static Scanner scanner = new Scanner(System.in);
    private static UserDAO userDAO = new UserDAO();
    private static GroupDAO groupDAO = new GroupDAO();
    private static GroupMemberDAO groupMemberDAO = new GroupMemberDAO();
    private static BalanceService balanceService = new BalanceService();
    private static SettlementService settlementService = new SettlementService();
    private static ExpenseDAO expenseDAO = new ExpenseDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n--- Smart Expense Splitter ---");
            System.out.println("1. Add User");
            System.out.println("2. List Users");
            System.out.println("3. Add Group");
            System.out.println("4. List Groups");
            System.out.println("5. Add User to Group");
            System.out.println("6. List Group Members");
            System.out.println("7. Add Expense");
            System.out.println("8. View Balances");
            System.out.println("9. View Transaction History");
            System.out.println("10. Exit");

            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 ->
                    addUser();
                case 2 ->
                    listUsers();
                case 3 ->
                    addGroup();
                case 4 ->
                    listGroups();
                case 5 ->
                    addUserToGroup();
                case 6 ->
                    listGroupMembers();
                case 7 ->
                    addExpense();
                case 8 ->
                    viewBalances();
                case 9 ->
                    viewTransactionHistory();

                case 10 -> {
                    System.out.println("👋 Exiting...");
                    System.exit(0);
                }
                default ->
                    System.out.println("⚠️ Invalid option. Try again.");
            }
        }
    }

    private static void addUser() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        User user = new User(name, email);
        userDAO.addUser(user);
    }

    private static void listUsers() {
        List<User> users = userDAO.getAllUsers();
        System.out.println("\n--- All Users ---");
        for (User u : users) {
            System.out.println("ID: " + u.getUserId() + " | Name: " + u.getName() + " | Email: " + u.getEmail());
        }
    }

    private static void addGroup() {
        System.out.print("Enter group name: ");
        String groupName = scanner.nextLine();
        ExpenseGroup group = new ExpenseGroup(groupName);
        groupDAO.addGroup(group);
    }

    private static void listGroups() {
        List<ExpenseGroup> groups = groupDAO.getAllGroups();
        System.out.println("\n--- All Groups ---");
        for (ExpenseGroup g : groups) {
            System.out.println("ID: " + g.getGroupId() + " | Name: " + g.getGroupName());
        }
    }

    // In MainApp.java: replace addExpense() with below version
    private static void addExpense() {
        System.out.print("Enter Group ID: ");
        int groupId = scanner.nextInt();
        System.out.print("Enter Payer User ID: ");
        int userId = scanner.nextInt();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Description: ");
        String description = scanner.nextLine();
        System.out.print("Enter Category (e.g., Food, Travel, Utilities): ");
        String category = scanner.nextLine();

        // get members for the group to build splits
        List<GroupMember> members = groupMemberDAO.getMembersByGroup(groupId);
        if (members.isEmpty()) {
            System.out.println("⚠️ This group has no members. Add members first.");
            return;
        }

        System.out.println("Split type: 1) Equal  2) Percentage-based  3) Custom amounts");
        System.out.print("Choose (1/2/3): ");
        int splitType = scanner.nextInt();
        scanner.nextLine();

        List<ExpenseSplit> splits = new ArrayList<>();

        if (splitType == 1) {
            // equal split among group members
            double perHead = Math.round((amount / members.size()) * 100.0) / 100.0;
            // adjust last member to absorb rounding diff
            double totalAssigned = 0.0;
            for (int i = 0; i < members.size(); i++) {
                GroupMember gm = members.get(i);
                double assigned = (i == members.size() - 1) ? Math.round((amount - totalAssigned) * 100.0) / 100.0 : perHead;
                totalAssigned += assigned;
                splits.add(new ExpenseSplit(0, gm.getUserId(), assigned, Math.round((assigned / amount) * 10000.0) / 100.0));
            }
        } else if (splitType == 2) {
            // percentage-based: ask percent per user (sum must be ~100)
            double totalPercent = 0.0;
            for (GroupMember gm : members) {
                User u = userDAO.getUserById(gm.getUserId());
                String name = (u != null) ? u.getName() : String.valueOf(gm.getUserId());
                System.out.printf("Enter percentage for %s: ", name);
                double p = scanner.nextDouble();
                scanner.nextLine();
                totalPercent += p;
                double assigned = Math.round((p / 100.0 * amount) * 100.0) / 100.0;
                splits.add(new ExpenseSplit(0, gm.getUserId(), assigned, p));
            }
            if (Math.abs(totalPercent - 100.0) > 0.5) {
                System.out.println("⚠️ Percentages don't sum to ~100. Aborting. Please try again.");
                return;
            }
            // adjust rounding diff
            double sumAssigned = splits.stream().mapToDouble(ExpenseSplit::getAmount).sum();
            double diff = Math.round((amount - sumAssigned) * 100.0) / 100.0;
            if (Math.abs(diff) >= 0.01) {
                // add diff to payer's split if present
                for (ExpenseSplit s : splits) {
                    if (s.getUserId() == userId) {
                        s = new ExpenseSplit(s.getExpenseId(), s.getUserId(), s.getAmount() + diff, s.getPercentage());
                        // replace in list
                        // find and replace by index
                        for (int k = 0; k < splits.size(); k++) {
                            if (splits.get(k).getUserId() == s.getUserId()) {
                                splits.set(k, s);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            // custom amounts: ask each member a rupee amount
            double totalAssigned = 0.0;
            for (int i = 0; i < members.size(); i++) {
                GroupMember gm = members.get(i);
                User u = userDAO.getUserById(gm.getUserId());
                String name = (u != null) ? u.getName() : String.valueOf(gm.getUserId());
                System.out.printf("Enter amount for %s: ", name);
                double assigned = scanner.nextDouble();
                scanner.nextLine();
                totalAssigned += assigned;
                splits.add(new ExpenseSplit(0, gm.getUserId(), assigned, Math.round((assigned / amount) * 10000.0) / 100.0));
            }
            if (Math.abs(totalAssigned - amount) > 0.01) {
                System.out.println("⚠️ Sum of custom amounts doesn't equal total. Aborting.");
                return;
            }
        }

        Expense expense = new Expense(groupId, userId, amount, description, category);
        try {
            expenseDAO.addExpenseWithSplits(expense, splits);
        } catch (Exception e) {
            System.out.println("❌ Could not add expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewBalances() {
        System.out.print("Enter Group ID: ");
        int groupId = scanner.nextInt();
        scanner.nextLine();

        var balances = balanceService.calculateBalances(groupId);
        System.out.println("\n--- Group Balances ---");
        for (var entry : balances.entrySet()) {
            User u = userDAO.getUserById(entry.getKey());
            String name = (u != null) ? u.getName() : "Unknown";
            double balance = entry.getValue();

            if (balance > 0) {
                System.out.printf("%s should receive ₹%.2f\n", name, balance);
            } else if (balance < 0) {
                System.out.printf("%s should pay ₹%.2f\n", name, -balance);
            } else {
                System.out.printf("%s is settled up.\n", name);
            }
        }

        System.out.println("\n💰 Settlement Summary:");
        var transactions = settlementService.settleUp(balances, userDAO);
        if (transactions.isEmpty()) {
            System.out.println("Everyone is settled up!");
        } else {
            for (var t : transactions) {
                System.out.println(t);
            }
        }
    }

    private static void addUserToGroup() {
        System.out.print("Enter Group ID: ");
        int groupId = scanner.nextInt();
        System.out.print("Enter User ID: ");
        int userId = scanner.nextInt();
        scanner.nextLine();

        groupMemberDAO.addMemberToGroup(groupId, userId);
    }

    private static void listGroupMembers() {
        System.out.print("Enter Group ID: ");
        int groupIdToView = scanner.nextInt();
        scanner.nextLine();

        List<GroupMember> members = groupMemberDAO.getMembersByGroup(groupIdToView);
        System.out.println("\n--- Group Members ---");
        for (GroupMember m : members) {
            User u = userDAO.getUserById(m.getUserId());
            if (u != null) {
                System.out.println("Member ID: " + m.getId() + " | User ID: " + u.getUserId() + " | Name: " + u.getName());
            } else {
                System.out.println("Member ID: " + m.getId() + " | User ID: " + m.getUserId());
            }
        }
    }

    private static void viewTransactionHistory() {
        System.out.print("Enter Group ID: ");
        int groupId = scanner.nextInt();
        scanner.nextLine();

        List<Expense> expenses = expenseDAO.getExpensesByGroup(groupId);
        if (expenses.isEmpty()) {
            System.out.println("No transaction history found for this group.");
            return;
        }

        System.out.println("\n--- Transaction History ---");
        for (Expense expense : expenses) {
            User payer = userDAO.getUserById(expense.getPaidByUserId());
            String payerName = (payer != null) ? payer.getName() : "Unknown";
            System.out.printf("Expense ID: %d | Payer: %s | Amount: ₹%.2f | Description: %s | Category: %s\n",
                    expense.getExpenseId(), payerName, expense.getAmount(), expense.getDescription(), expense.getCategory());
        }
    }
}
