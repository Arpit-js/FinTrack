package com.fynance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceCalculator {

    public static void calculateBalances(int groupId, GroupMemberDAO groupMemberDAO, ExpenseDAO expenseDAO, UserDAO userDAO) {
        List<GroupMember> members = groupMemberDAO.getMembersByGroup(groupId);
        List<Expense> expenses = expenseDAO.getExpensesByGroup(groupId);

        if (members.isEmpty()) {
            System.out.println("⚠️ No members found in this group.");
            return;
        }
        if (expenses.isEmpty()) {
            System.out.println("⚠️ No expenses found for this group.");
            return;
        }

        // Initialize balances
        Map<Integer, Double> balanceMap = new HashMap<>();
        for (GroupMember m : members) {
            balanceMap.put(m.getUserId(), 0.0);
        }

        // Total expense and equal share
        double totalExpense = 0;
        for (Expense e : expenses) {
            totalExpense += e.getAmount();
            balanceMap.put(e.getPaidByUserId(), balanceMap.getOrDefault(e.getPaidByUserId(), 0.0) + e.getAmount());
        }

        double share = totalExpense / members.size();

        System.out.println("\n--- 💰 Balance Summary ---");
        for (GroupMember m : members) {
            User u = userDAO.getUserById(m.getUserId());
            double netBalance = balanceMap.get(m.getUserId()) - share;

            if (netBalance > 0) {
                System.out.println(u.getName() + " should get back ₹" + String.format("%.2f", netBalance));
            } else if (netBalance < 0) {
                System.out.println(u.getName() + " owes ₹" + String.format("%.2f", Math.abs(netBalance)));
            } else {
                System.out.println(u.getName() + " is settled up ✅");
            }
        }
    }
}
