package com.fynance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceService {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final GroupMemberDAO groupMemberDAO = new GroupMemberDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();

    public Map<Integer, Double> calculateBalances(int groupId) {
        Map<Integer, Double> balances = new HashMap<>();

        // Initialize balances for all group members
        List<GroupMember> members = groupMemberDAO.getMembersByGroup(groupId);
        if (members.isEmpty()) {
            return balances; // No members, no balances
        }
        for (GroupMember member : members) {
            balances.put(member.getUserId(), 0.0);
        }

        // 1. Calculate balances from expenses
        List<Expense> expenses = expenseDAO.getExpensesByGroup(groupId);
        double totalExpense = 0;
        for (Expense expense : expenses) {
            totalExpense += expense.getAmount();
            balances.put(expense.getPaidByUserId(), balances.getOrDefault(expense.getPaidByUserId(), 0.0) + expense.getAmount());
        }

        // Calculate each person's share and adjust balances
        if (totalExpense > 0) {
            double equalShare = totalExpense / members.size();
            for (Integer userId : balances.keySet()) {
                balances.put(userId, balances.get(userId) - equalShare);
            }
        }

        // 2. Adjust balances with recorded settlements
        List<Settlement> settlements = settlementDAO.getSettlementsForGroup(groupId);
        for (Settlement settlement : settlements) {
            // Subtract from the payer
            balances.put(settlement.getFromUserId(), balances.get(settlement.getFromUserId()) - settlement.getAmount());
            // Add to the receiver
            balances.put(settlement.getToUserId(), balances.get(settlement.getToUserId()) + settlement.getAmount());
        }

        return balances;
    }
}
